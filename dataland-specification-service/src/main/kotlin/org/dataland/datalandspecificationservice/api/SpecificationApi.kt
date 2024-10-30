package org.dataland.datalandspecificationservice.api

import io.swagger.v3.oas.annotations.Operation
import org.dataland.datalandspecificationservice.model.DataPointSpecificationDto
import org.dataland.datalandspecificationservice.model.DataPointTypeSpecificationDto
import org.dataland.datalandspecificationservice.model.FrameworkSpecificationDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

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
}
