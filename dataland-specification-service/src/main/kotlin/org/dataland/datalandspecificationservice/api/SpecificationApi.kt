package org.dataland.datalandspecificationservice.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.dataland.datalandspecificationservice.model.DataPointSpecificationDto
import org.dataland.datalandspecificationservice.model.DataPointTypeSpecificationDto
import org.dataland.datalandspecificationservice.model.FrameworkSpecificationDto
import org.dataland.datalandspecificationservice.model.SimpleFrameworkSpecificationDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

/**
 * API for the specification service
 */
interface SpecificationApi {
    /**
     * Get the framework specification for a given id
     */
    @Operation(
        summary = "Get the framework specification for a given id.",
        description = "Get the framework specification for a given id.",
    )
    @GetMapping(
        value = ["/frameworks/{frameworkSpecificationId}"],
        produces = ["application/json"],
    )
    fun getFrameworkSpecification(
        @PathVariable("frameworkSpecificationId") frameworkSpecificationId: String,
    ): ResponseEntity<FrameworkSpecificationDto>

    /**
     * A method to check if a companyId is valid
     * @param companyId the identifier
     */
    @Operation(
        summary = "Checks if a framework specification exists.",
        description = "Checks if a framework specification exists.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully checked that the framework is known by dataland.",
            ),
            ApiResponse(
                responseCode = "404",
                description = "Framework is not known by dataland.",
            ),
        ],
    )
    @RequestMapping(
        method = [RequestMethod.HEAD],
        value = ["/frameworks/{frameworkSpecificationId}"],
    )
    fun doesFrameworkSpecificationExist(
        @PathVariable("frameworkSpecificationId") frameworkSpecificationId: String,
    )

    /**
     * Get the framework specification for a given id
     */
    @Operation(
        summary = "List all framework specifications",
        description = "List all framework specifications",
    )
    @GetMapping(
        value = ["/frameworks"],
        produces = ["application/json"],
    )
    fun listFrameworkSpecifications(): ResponseEntity<List<SimpleFrameworkSpecificationDto>>

    /**
     * Get the data point specification for a given id
     */
    @Operation(
        summary = "Get the data point specification for a given id.",
        description = "Get the data point specification for a given id.",
    )
    @GetMapping(
        value = ["/data-points/{dataPointSpecificationId}"],
        produces = ["application/json"],
    )
    fun getDataPointSpecification(
        @PathVariable("dataPointSpecificationId") dataPointSpecificationId: String,
    ): ResponseEntity<DataPointSpecificationDto>

    /**
     * Get the data type specification for a given id
     */
    @Operation(
        summary = "Get the data type specification for a given id.",
        description = "Get the data type specification for a given id.",
    )
    @GetMapping(
        value = ["/data-point-types/{dataPointTypeSpecificationId}"],
        produces = ["application/json"],
    )
    fun getDataPointTypeSpecification(
        @PathVariable("dataPointTypeSpecificationId") dataPointTypeSpecificationId: String,
    ): ResponseEntity<DataPointTypeSpecificationDto>

    /**
     * Get the java class that validates a data point
     */
    @Operation(
        summary = "Get the kotlin class that validates the data point type.",
        description = "Get the kotlin class that validates the data point type.",
    )
    @GetMapping(
        value = ["/data-point-types/{dataPointTypeSpecificationId}/validated-by"],
        produces = ["text/plain", "*/*"],
    )
    fun getKotlinClassValidatingTheDataPointType(
        @PathVariable("dataPointTypeSpecificationId") dataPointTypeSpecificationId: String,
    ): ResponseEntity<String>

    /**
     * Get the java class that validates a data point
     */
    @Operation(
        summary = "Get the kotlin class that validates the data point.",
        description = "Get the kotlin class that validates the data point.",
    )
    @GetMapping(
        value = ["/data-points/{dataPointSpecificationId}/validated-by"],
        produces = ["text/plain", "*/*"],
    )
    fun getKotlinClassValidatingTheDataPoint(
        @PathVariable("dataPointSpecificationId") dataPointSpecificationId: String,
    ): ResponseEntity<String>
}
