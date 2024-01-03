package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.dataland.datalandbackend.model.companies.CompanyDataOwners
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * Defines the restful dataland-backend API regarding (company) data ownership.
 */
@RequestMapping("/data-owners")
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
interface DataOwnerApi {

    /**
     * A method to create a new data ownership relation in dataland
     * @param companyId the ID of the company to which a new data owner is to be added
     * @param userId the ID of the user who is to be added as company data owner
     * @return information about the stored company data ownership relation
     */
    @Operation(
        summary = "Add a new data owner to a company.",
        description = "A new data owner is added to the existing list for the specified company.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully added data owner."),
        ],
    )
    @PostMapping(
        produces = ["application/json"],
        consumes = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun postDataOwner(
        @RequestParam companyId: String,
        @RequestParam userId: String,
    ):
        ResponseEntity<CompanyDataOwners>
}
