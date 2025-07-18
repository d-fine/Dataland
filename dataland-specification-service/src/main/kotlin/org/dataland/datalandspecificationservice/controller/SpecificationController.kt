package org.dataland.datalandspecificationservice.controller

import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandspecification.database.SpecificationDatabase
import org.dataland.datalandspecification.specifications.DataPointBaseType
import org.dataland.datalandspecification.specifications.DataPointType
import org.dataland.datalandspecificationservice.api.SpecificationApi
import org.dataland.datalandspecificationservice.model.DataPointBaseTypeSchema
import org.dataland.datalandspecificationservice.model.DataPointBaseTypeSpecification
import org.dataland.datalandspecificationservice.model.DataPointTypeSpecification
import org.dataland.datalandspecificationservice.model.FrameworkSpecification
import org.dataland.datalandspecificationservice.model.SimpleFrameworkSpecification
import org.dataland.datalandspecificationservice.model.getRef
import org.dataland.datalandspecificationservice.model.toDto
import org.dataland.datalandspecificationservice.model.toSimpleDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import kotlin.text.get

/**
 * Controller for the specification service
 */
@RestController
class SpecificationController(
    @Autowired val database: SpecificationDatabase,
    @Value("\${dataland.primary-url}") val datalandPrimaryUrl: String,
) : SpecificationApi {
    private fun getRawDataPointSpecification(dataPointSpecificationId: String): DataPointType =
        database.dataPointTypes[dataPointSpecificationId]
            ?: throw ResourceNotFoundApiException(
                "Data Point Specification with id $dataPointSpecificationId not found",
                "The data point specification with the given id was not found in the database.",
            )

    private fun getRawDataPointBaseType(dataPointBaseTypeId: String): DataPointBaseType =
        database.dataPointBaseTypes[dataPointBaseTypeId]
            ?: throw ResourceNotFoundApiException(
                "Data point base type with id $dataPointBaseTypeId not found",
                "The data point base type with the given id was not found in the database.",
            )

    override fun doesFrameworkSpecificationExist(frameworkSpecificationId: String) {
        if (!database.frameworks.containsKey(frameworkSpecificationId)) {
            throw ResourceNotFoundApiException(
                "Framework Specification with id $frameworkSpecificationId not found",
                "The framework specification with the given id was not found in the database.",
            )
        }
    }

    override fun listFrameworkSpecifications(): ResponseEntity<List<SimpleFrameworkSpecification>> =
        ResponseEntity
            .ok(database.frameworks.values.map { it.toSimpleDto(datalandPrimaryUrl) })

    override fun getFrameworkSpecification(frameworkSpecificationId: String): ResponseEntity<FrameworkSpecification> {
        val frameworkSpecification =
            database.frameworks[frameworkSpecificationId]
                ?: throw ResourceNotFoundApiException(
                    "Framework Specification with id $frameworkSpecificationId not found",
                    "The framework specification with the given id was not found in the database.",
                )
        return ResponseEntity.ok(frameworkSpecification.toDto(datalandPrimaryUrl, database))
    }

    override fun getDataPointTypeSpecification(dataPointTypeId: String): ResponseEntity<DataPointTypeSpecification> {
        val dataPointSpecification = getRawDataPointSpecification(dataPointTypeId)
        return ResponseEntity.ok(dataPointSpecification.toDto(datalandPrimaryUrl, database))
    }

    override fun getDataPointBaseType(dataPointBaseTypeId: String): ResponseEntity<DataPointBaseTypeSpecification> {
        val dataPointBaseType = getRawDataPointBaseType(dataPointBaseTypeId)
        return ResponseEntity.ok(dataPointBaseType.toDto(datalandPrimaryUrl, database))
    }

    override fun getKotlinClassValidatingTheDataPointBaseType(dataPointBaseTypeId: String): ResponseEntity<String> {
        val dataPointBaseType = getRawDataPointBaseType(dataPointBaseTypeId)
        return ResponseEntity.ok(dataPointBaseType.validatedBy)
    }

    override fun getKotlinClassValidatingTheDataPointType(dataPointSpecificationId: String): ResponseEntity<String> {
        val dataPointSpecification = getRawDataPointSpecification(dataPointSpecificationId)
        val dataPointBaseType = getRawDataPointBaseType(dataPointSpecification.dataPointBaseTypeId)
        return ResponseEntity.ok(dataPointBaseType.validatedBy)
    }

    override fun getDataPointBaseTypeSchema(frameworkSpecificationId: String): ResponseEntity<DataPointBaseTypeSchema> {
        val framework =
            database.frameworks[frameworkSpecificationId]
                ?: throw ResourceNotFoundApiException(
                    "Framework Specification with id $frameworkSpecificationId not found",
                    "The framework specification with the given id was not found in the database.",
                )
        val resolvedSchema = resolveSchema(framework.schema, database)
        val dto =
            DataPointBaseTypeSchema(
                framework = framework.getRef(datalandPrimaryUrl),
                name = framework.name,
                businessDefinition = framework.businessDefinition,
                resolvedSchema = resolvedSchema,
                referencedReportJsonPath = framework.referencedReportJsonPath,
            )
        return ResponseEntity.ok(dto)
    }

    private fun resolveSchema(
        schema: com.fasterxml.jackson.databind.JsonNode,
        database: SpecificationDatabase,
    ): Any =
        when {
            schema.isObject ->
                schema.fields().asSequence().associate { (key, value) ->
                    key to resolveSchema(value, database)
                }

            schema.isArray -> schema.map { resolveSchema(it, database) }

            schema.isTextual -> {
                val typeId = schema.asText()
                val dataPointType = database.dataPointTypes[typeId]
                if (dataPointType != null) {
                    val baseType = database.dataPointBaseTypes[dataPointType.dataPointBaseTypeId]
                    if (baseType?.schema != null) {
                        resolveSchema(baseType.schema, database)
                    } else {
                        baseType?.validatedBy ?: typeId
                    }
                } else {
                    typeId
                }
            }

            else -> schema
        }
}
