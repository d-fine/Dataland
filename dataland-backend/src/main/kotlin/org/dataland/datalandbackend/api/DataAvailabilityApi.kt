package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.dataland.datalandbackend.model.DataDimensionSearchRequest
import org.dataland.datalandbackendutils.model.BasicDataDimensions
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

/**
 * Defines the restful dataland-backend API for data availability queries.
 */
@RequestMapping("/data-availability")
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
interface DataAvailabilityApi {
    /**
     * Filters the provided exact data dimension triples to those for which viewable data exists.
     * @param dimensions list of (companyId, dataType, reportingPeriod) triples to filter
     */
    @Operation(
        summary = "Filters the provided data dimension triples to those with viewable data.",
        description =
            "Accepts a list of exact (companyId, dataType, reportingPeriod) triples and returns " +
                "those for which viewable data exists, covering both datasets and data points.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved viewable data dimensions."),
        ],
    )
    @PostMapping(value = ["/viewable-dimensions/filter"])
    @PreAuthorize("hasRole('ROLE_USER')")
    fun filterViewableDimensions(
        @RequestBody dimensions: List<BasicDataDimensions>,
    ): ResponseEntity<List<BasicDataDimensions>>

    /**
     * Searches for all viewable data dimensions matching the given filters.
     * @param request filter containing companyIds, dataTypes, and reportingPeriods;
     *   an empty list for any field is treated as a wildcard (all values)
     */
    @Operation(
        summary = "Returns all viewable data dimensions matching the given filters.",
        description =
            "Accepts lists of company IDs, frameworks or data point types, and reporting periods. " +
                "Returns all viewable data dimensions that match any combination of the provided filters. " +
                "An empty list for any field is treated as a wildcard and matches all values for that dimension.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved viewable data dimensions."),
        ],
    )
    @PostMapping(value = ["/viewable-dimensions/search"])
    @PreAuthorize("hasRole('ROLE_USER')")
    fun searchViewableDimensions(
        @RequestBody request: DataDimensionSearchRequest,
    ): ResponseEntity<List<BasicDataDimensions>>
}
