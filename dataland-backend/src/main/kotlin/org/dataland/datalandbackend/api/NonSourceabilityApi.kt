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
 * Defines the restful dataland-backend API for bulk non-sourceability queries.
 */
@RequestMapping("/non-sourceable")
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
interface NonSourceabilityApi {
    /**
     * Searches for all currently-active non-sourceability triples matching the given filters.
     * @param request filter containing companyIds, dataTypes, and reportingPeriods;
     *   an empty list for any field is treated as a wildcard (all values)
     */
    @Operation(
        summary = "Returns all currently-active non-sourceability data dimensions matching the given filters.",
        description =
            "Accepts lists of company IDs, frameworks, and reporting periods. " +
                "Returns the set of (companyId, dataType, reportingPeriod) triples for which an active " +
                "non-sourceability entry exists and that match any combination of the provided filters. " +
                "An empty list for any field is treated as a wildcard and matches all values for that dimension.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved non-sourceable data dimensions."),
        ],
    )
    @PostMapping(value = ["/search"])
    @PreAuthorize("hasRole('ROLE_USER')")
    fun searchNonSourceableDimensions(
        @RequestBody request: DataDimensionSearchRequest,
    ): ResponseEntity<Set<BasicDataDimensions>>

    /**
     * Searches for all currently-active non-sourceability triples matching the given filters and
     * groups the results by company ID and framework (data type).
     * @param request filter containing companyIds, dataTypes, and reportingPeriods;
     *   an empty list for any field is treated as a wildcard (all values)
     */
    @Operation(
        summary =
            "Returns all currently-active non-sourceability data dimensions matching the given filters, " +
                "grouped by company ID and framework.",
        description =
            "Accepts lists of company IDs, frameworks, and reporting periods. " +
                "Returns the set of (companyId, dataType, reportingPeriod) triples for which an active " +
                "non-sourceability entry exists and that match any combination of the provided filters, " +
                "pre-grouped into a map of maps: companyId -> framework -> set of matching data dimensions. " +
                "An empty list for any field is treated as a wildcard and matches all values for that dimension.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved non-sourceable data dimensions grouped by company and framework.",
            ),
        ],
    )
    @PostMapping(value = ["/search/grouped"])
    @PreAuthorize("hasRole('ROLE_USER')")
    fun searchNonSourceableDimensionsGroupedByCompanyAndFramework(
        @RequestBody request: DataDimensionSearchRequest,
    ): ResponseEntity<Map<String, Map<String, Set<BasicDataDimensions>>>>
}
