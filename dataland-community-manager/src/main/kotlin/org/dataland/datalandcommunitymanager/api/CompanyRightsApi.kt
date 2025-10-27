package org.dataland.datalandcommunitymanager.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.dataland.datalandcommunitymanager.model.companyRights.CompanyRight
import org.dataland.datalandcommunitymanager.model.companyRights.CompanyRightAssignment
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
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
                content = [Content(array = ArraySchema())],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Dataland does not know the specified company ID.",
                content = [Content(array = ArraySchema())],
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

    /**
     * Post a company right assignment.
     */
    @Operation(
        summary = "Post a company right assignment.",
        description = "Assign a company right to a Dataland company, specified by its ID.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully assigned the company right."),
            ApiResponse(
                responseCode = "403",
                description = "Only Dataland admins may assign company rights.",
                content = [Content(array = ArraySchema())],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Dataland does not know the specified company ID.",
                content = [Content(array = ArraySchema())],
            ),
        ],
    )
    @PostMapping(
        consumes = ["application/json"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun postCompanyRight(
        @RequestBody
        companyRightAssignment: CompanyRightAssignment<String>,
    ): ResponseEntity<CompanyRightAssignment<String>>

    /**
     * Delete a company right assignment.
     */
    @Operation(
        summary = "Delete a company right assignment.",
        description = "Remove a company right from a Dataland company.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully deleted the company right assignment."),
            ApiResponse(
                responseCode = "403",
                description = "Only Dataland admins may remove company rights.",
                content = [Content(schema = Schema())],
            ),
            ApiResponse(
                responseCode = "404",
                description = "The company right assignment which you want to delete does not exist.",
                content = [Content(schema = Schema())],
            ),
        ],
    )
    @DeleteMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun deleteCompanyRight(
        @RequestBody
        companyRightAssignment: CompanyRightAssignment<String>,
    ): ResponseEntity<Unit>
}
