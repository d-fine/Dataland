package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.dataland.datalandbackend.model.dataavailability.DataAvailabilitySearchRequest
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
     * Checks which of the provided exact data dimension triples have active data.
     * @param dimensions list of (companyId, dataType, reportingPeriod) triples to check
     */
    @Operation(
        summary = "Returns which of the provided data dimension triples have active data.",
        description =
            "Accepts a list of exact (companyId, dataType, reportingPeriod) triples and returns " +
                "those for which active data exists, covering both datasets and data points.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved active data dimensions."),
        ],
    )
    @PostMapping(value = ["/active-dimensions-search"])
    @PreAuthorize("hasRole('ROLE_USER')")
    fun getActiveDimensions(
        @RequestBody dimensions: List<BasicDataDimensions>,
    ): ResponseEntity<List<BasicDataDimensions>>

    /**
     * Returns all available data dimensions matching the given filters.
     * @param request filter containing companyIds (required), frameworksOrDataPointTypes (required),
     *   and reportingPeriods (optional; empty list means all periods)
     */
    @Operation(
        summary = "Returns all available data dimensions matching the given filters.",
        description =
            "Accepts lists of company IDs, frameworks or data point types, and reporting periods. " +
                "Returns all active data dimensions that match any combination of the provided filters. " +
                "companyIds and frameworksOrDataPointTypes must each contain at least one entry. " +
                "An empty reportingPeriods list is treated as a wildcard and matches all reporting periods.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved available data dimensions."),
            ApiResponse(
                responseCode = "400",
                description = "companyIds and frameworksOrDataPointTypes must each contain at least one entry.",
            ),
        ],
    )
    @PostMapping(value = ["/available-data-dimensions"])
    @PreAuthorize("hasRole('ROLE_USER')")
    fun getAvailableDataDimensions(
        @RequestBody request: DataAvailabilitySearchRequest,
    ): ResponseEntity<List<BasicDataDimensions>>
}
