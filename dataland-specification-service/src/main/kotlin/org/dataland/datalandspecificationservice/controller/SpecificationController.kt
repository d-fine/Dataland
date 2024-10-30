package org.dataland.datalandspecificationservice.controller

import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandspecification.database.SpecificationDatabase
import org.dataland.datalandspecificationservice.api.SpecificationApi
import org.dataland.datalandspecificationservice.model.DataPointSpecificationDto
import org.dataland.datalandspecificationservice.model.DataPointTypeSpecificationDto
import org.dataland.datalandspecificationservice.model.FrameworkSpecificationDto
import org.dataland.datalandspecificationservice.model.toDto
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
    override fun getFrameworkSpecification(frameworkSpecificationId: String): ResponseEntity<FrameworkSpecificationDto> {
        val frameworkSpecification =
            database.frameworkSpecifications[frameworkSpecificationId]
                ?: throw ResourceNotFoundApiException(
                    "Framework Specification with id $frameworkSpecificationId not found",
                    "The framework specification with the given id was not found in the database.",
                )
        return ResponseEntity.ok(frameworkSpecification.toDto(datalandPrimaryUrl, database))
    }

    override fun getDataPointSpecification(dataPointSpecificationId: String): ResponseEntity<DataPointSpecificationDto> {
        val dataPointSpecification =
            database.dataPointSpecifications[dataPointSpecificationId]
                ?: throw ResourceNotFoundApiException(
                    "Data Point Specification with id $dataPointSpecificationId not found",
                    "The data point specification with the given id was not found in the database.",
                )
        return ResponseEntity.ok(dataPointSpecification.toDto(datalandPrimaryUrl, database))
    }

    override fun getDataPointTypeSpecification(dataPointTypeSpecificationId: String): ResponseEntity<DataPointTypeSpecificationDto> {
        val dataPointSpecification =
            database.dataPointTypeSpecifications[dataPointTypeSpecificationId]
                ?: throw ResourceNotFoundApiException(
                    "Data Point Type Specification with id $dataPointTypeSpecificationId not found",
                    "The data point type specification with the given id was not found in the database.",
                )
        return ResponseEntity.ok(dataPointSpecification.toDto(datalandPrimaryUrl, database))
    }
}
