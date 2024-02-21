package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.dataland.datalandbackend.model.companies.CompanyDataOwners
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import java.util.UUID

/**
 * Defines the restful dataland-backend API regarding (company) data ownership.
 */
@RequestMapping("/companies")
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
        value = ["/{companyId}/data-owners/{userId}"],

        )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun postDataOwner(
        @PathVariable("companyId") companyId: UUID,
        @PathVariable("userId") userId: UUID,
    ):
            ResponseEntity<CompanyDataOwners>

    /**
     * A method to retrieve a  data owner information from companies in dataland
     * @param companyId the ID of the company to which a new data owner is to be added
     * @return userId of the data owner(s) of a specified company
     */
    @Operation(
        summary = "Retrieve data owner(s) of a company.",
        description = "Get a list of data owner(s) for the specified company.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved data owner."),
            ApiResponse(responseCode = "404", description = "The specified company does not exist on Dataland."),
        ],
    )
    @GetMapping(
        produces = ["application/json"],
        value = ["/{companyId}/data-owners"],

        )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun getDataOwners(
        @PathVariable("companyId") companyId: UUID,
    ):
            ResponseEntity<List<String>>

    /**
     * A method to delete a data ownership relation in dataland
     * @param companyId the ID of the company to which the data ownership should be removed from
     * @param userId the ID of the user who is to be removed as company data owner
     * @return information about the deleted company data ownership relation
     */
    @Operation(
        summary = "Delete a data owner from a specified company.",
        description = "An existing data owner is deleted from the existing list for the specified company.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully deleted data owner."),
        ],
    )
    @DeleteMapping(
        produces = ["application/json"],
        value = ["/{companyId}/data-owners/{userId}"],

        )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun deleteDataOwner(
        @PathVariable("companyId") companyId: UUID,
        @PathVariable("userId") userId: UUID,
    ):
            ResponseEntity<CompanyDataOwners>

    /**
     * A method to check if a user specified via their ID is data owner for a certain company
     * @param companyId the ID of the company
     * @param userId the ID of the user
     */
    @Operation(
        summary = "Validation of a user-company combination with regards to data ownership.",
        description = "Checks whether a user is data owner of a company.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "The specified user is data owner of the company."),
            ApiResponse(
                responseCode = "404",
                description = "Either the specified company does not exist on Dataland or the user isn't data owner " +
                        "of that company.",
            ),
        ],
    )
    @RequestMapping(
        method = [RequestMethod.HEAD],
        value = ["/{companyId}/data-owners/{userId}"],
    )
    @PreAuthorize("hasRole('ROLE_ADMIN') or @SecurityUtilsService.amIAskingForMySelf(#userId)")
    fun isUserDataOwnerForCompany(
        @PathVariable("companyId") companyId: UUID,
        @PathVariable("userId") userId: UUID,
    )

    /**
     * A method to request data ownership on a specified company for the current user
     * @param companyId the ID of the company for which data ownership is requested
     */
    @Operation(
        summary = "Request data ownership for a company.",
        description = "Request data ownership for one of the existing company on Dataland.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully requested data ownership."),
        ],
    )
    @PostMapping(
        value = ["/{companyId}/data-ownership-requests"],
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    fun postDataOwnershipRequest(
        @PathVariable("companyId") companyId: UUID,
        @RequestParam comment: String? = null,
    )

    /**
     * A method to verify if a company has data owners
     * @param companyId the ID of the company for which the information should be retrieved
     */
    @Operation(
        summary = "Validation if data owners exists for the specified company.",
        description = "Checks whether company has data owners or not",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "The specified company has data owner(s)."),
            ApiResponse(
                responseCode = "404",
                description = "The specified company has no data owner(s) yet",
            ),
        ],
    )
    @RequestMapping(
        method = [RequestMethod.HEAD],
        value = ["/{companyId}/data-owners"],
    )
    fun hasCompanyDataOwner(
        @PathVariable("companyId") companyId: UUID,
    )
}
