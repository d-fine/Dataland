package org.dataland.datalandspecificationservice.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.dataland.datalandspecificationservice.model.DataPointBaseTypeSpecification
import org.dataland.datalandspecificationservice.model.DataPointTypeSpecification
import org.dataland.datalandspecificationservice.model.FrameworkSpecification
import org.dataland.datalandspecificationservice.model.SimpleFrameworkSpecification
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
    ): ResponseEntity<FrameworkSpecification>

    /**
     * A method to check if a framework is valid
     * @param frameworkSpecificationId the identifier
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
     * Get the list of simple framework specifications
     */
    @Operation(
        summary = "List all framework specifications",
        description = "List all framework specifications",
    )
    @GetMapping(
        value = ["/frameworks"],
        produces = ["application/json"],
    )
    fun listFrameworkSpecifications(): ResponseEntity<List<SimpleFrameworkSpecification>>

    /**
     * Get the data point type specification for a given id
     */
    @Operation(
        summary = "Get the data point type specification for a given id.",
        description = "Get the data point type specification for a given id.",
    )
    @GetMapping(
        value = ["/data-point-types/{dataPointTypeId}"],
        produces = ["application/json"],
    )
    fun getDataPointTypeSpecification(
        @PathVariable("dataPointTypeId") dataPointTypeId: String,
    ): ResponseEntity<DataPointTypeSpecification>

    /**
     * Get the data point base type specification for a given id
     */
    @Operation(
        summary = "Get the data point base type for a given id.",
        description = "Get the data point base type for a given id.",
    )
    @GetMapping(
        value = ["/data-point-base-types/{dataPointBaseTypeId}"],
        produces = ["application/json"],
    )
    fun getDataPointBaseType(
        @PathVariable("dataPointBaseTypeId") dataPointBaseTypeId: String,
    ): ResponseEntity<DataPointBaseTypeSpecification>

    /**
     * Get the kotlin data class that validates a data point base type
     */
    @Operation(
        summary = "Get the kotlin class that validates the data point base type.",
        description = "Get the kotlin class that validates the data point base type.",
    )
    @GetMapping(
        value = ["/data-point-base-types/{dataPointBaseTypeId}/validated-by"],
        produces = ["text/plain", "*/*"],
    )
    fun getKotlinClassValidatingTheDataPointBaseType(
        @PathVariable("dataPointBaseTypeId") dataPointBaseTypeId: String,
    ): ResponseEntity<String>

    /**
     * Get the kotlin class that validates a data point type
     */
    @Operation(
        summary = "Get the kotlin class that validates the data point type.",
        description = "Get the kotlin class that validates the data point type.",
    )
    @GetMapping(
        value = ["/data-point-types/{dataPointTypeId}/validated-by"],
        produces = ["text/plain", "*/*"],
    )
    fun getKotlinClassValidatingTheDataPointType(
        @PathVariable("dataPointTypeId") dataPointSpecificationId: String,
    ): ResponseEntity<String>
}
