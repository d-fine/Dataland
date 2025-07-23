package org.dataland.datalandspecificationservice.controller

import com.fasterxml.jackson.databind.JsonNode
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandspecification.database.SpecificationDatabase
import org.dataland.datalandspecification.specifications.DataPointBaseType
import org.dataland.datalandspecification.specifications.DataPointType
import org.dataland.datalandspecificationservice.api.SpecificationApi
import org.dataland.datalandspecificationservice.model.DataPointBaseTypeResolvedSchema
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

/**
 * Controller for the specification service
 */
@RestController
class SpecificationController(
    @Autowired val database: SpecificationDatabase,
    @Value("\${dataland.primary-url}") val datalandPrimaryUrl: String,
) : SpecificationApi {
    val framworkNotFound = "The framework specification with the given id was not found in the database."

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
                framworkNotFound,
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
                    framworkNotFound,
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

    override fun getDataPointBaseTypeSchema(frameworkSpecificationId: String): ResponseEntity<DataPointBaseTypeResolvedSchema> {
        val framework =
            database.frameworks[frameworkSpecificationId]
                ?: throw ResourceNotFoundApiException(
                    "Framework Specification with id $frameworkSpecificationId not found",
                    framworkNotFound,
                )
        val resolvedSchema = resolveSchema(framework.schema)
        val dto =
            DataPointBaseTypeResolvedSchema(
                framework = framework.getRef(datalandPrimaryUrl),
                name = framework.name,
                businessDefinition = framework.businessDefinition,
                resolvedSchema = resolvedSchema,
                referencedReportJsonPath = framework.referencedReportJsonPath,
            )
        return ResponseEntity.ok(dto)
    }

    private fun resolveSchema(schema: JsonNode): Any =
        when {
            schema.isObject ->
                schema.properties().asSequence().associate { (key, value) ->
                    key to resolveSchema(value)
                }

            schema.isArray -> schema.map { resolveSchema(it) }

            schema.isTextual -> {
                val typeId = schema.asText()
                val dataPointType = database.dataPointTypes[typeId]
                requireNotNull(dataPointType) { "Datapoint type $typeId was not found." }
                val baseType = database.dataPointBaseTypes[dataPointType.dataPointBaseTypeId]
                requireNotNull(baseType) { "Base type ${dataPointType.dataPointBaseTypeId} was not found." }
                requireNotNull(baseType.schema) { "Base type $baseType has no schema definition." }
                baseType.schema
            }

            else -> schema
        }
}
