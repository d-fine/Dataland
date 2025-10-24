package org.dataland.datalandcommunitymanager.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.dataland.datalandcommunitymanager.model.companyRights.CompanyRight
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

/**
 * Defines restful Dataland Community Manager API regarding company rights.
 */
@RequestMapping("/company-rights")
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
interface CompanyRightsApi {
    /**
     * Get a list of all rights assigned to a Dataland company, specified via its ID.
     * @param companyId ID of the company for which the rights are requested
     */
    @Operation(
        summary = "Get company rights.",
        description = "Get a list of all rights assigned to a Dataland company, specified via its ID.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved the list of company rights.",
            ),
            ApiResponse(
                responseCode = "403",
                description = "Only Dataland admins may query company rights.",
            ),
            ApiResponse(
                responseCode = "404",
                description = "Dataland does not know the specified company ID.",
            ),
        ],
    )
    @GetMapping(
        value = ["/{companyId}"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun getCompanyRights(
        @PathVariable("companyId")
        companyId: String,
    ): ResponseEntity<List<CompanyRight>>
}
