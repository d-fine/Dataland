package org.dataland.datalandspecificationservice.controller

import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandspecification.database.SpecificationDatabase
import org.dataland.datalandspecification.specifications.DataPointBaseType
import org.dataland.datalandspecification.specifications.DataPointType
import org.dataland.datalandspecificationservice.api.SpecificationApi
import org.dataland.datalandspecificationservice.model.DataPointBaseTypeSpecification
import org.dataland.datalandspecificationservice.model.DataPointTypeSpecification
import org.dataland.datalandspecificationservice.model.FrameworkSpecification
import org.dataland.datalandspecificationservice.model.SimpleFrameworkSpecification
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
}
