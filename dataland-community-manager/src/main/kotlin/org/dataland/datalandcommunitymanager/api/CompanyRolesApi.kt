package org.dataland.datalandcommunitymanager.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.dataland.datalandcommunitymanager.model.companyRoles.CompanyRole
import org.dataland.datalandcommunitymanager.model.companyRoles.CompanyRoleAssignment
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
 * Defines the community manager API regarding the different company roles that users can have.
 */
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
interface CompanyRolesApi {

    /**
     * A method to give a user one of the defined company roles for a specific company
     * @param companyRole that shall be assigned to the user
     * @param companyId for which a company role shall be assigned to the user
     * @param userId that defines to whom the company role shall be assigned to
     * @returns the created company role assignment
     */
    @Operation(
        summary = "Assign company role for the company to the user.",
        description = "The company role for the specified company is being assigned to the user.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully assigned company role."),
        ],
    )
    @PostMapping(
        produces = ["application/json"],
        value = ["/company-role-assignments/{role}/{companyId}/{userId}"],

    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun assignCompanyRole(
        @PathVariable("role") companyRole: CompanyRole,
        @PathVariable("companyId") companyId: UUID,
        @PathVariable("userId") userId: UUID,
    ):
        ResponseEntity<CompanyRoleAssignment>

    /**
     * A method to retrieve company role assignments
     * @param companyRole for which the assignments shall be retrieved
     * @param companyId of the company for which the company role assignments are valid
     * @returns the company role assignments for the specified company role and company
     */
    @Operation(
        summary = "Retrieve company role assignments for company and company role.",
        description = "Get company role assignments for the specified company role and company.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved company role assignments."),
            ApiResponse(responseCode = "404", description = "The specified company does not exist on Dataland."),
        ],
    )
    @GetMapping(
        produces = ["application/json"],
        value = ["/company-role-assignments/{role}/{companyId}"],

    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun getCompanyRoleAssignments(
        @PathVariable("role") companyRole: CompanyRole,
        @PathVariable("companyId") companyId: UUID,
    ):
        ResponseEntity<List<CompanyRoleAssignment>>

    /**
     * A method to remove the assignment of a company role from a user
     * @param companyRole that shall be removed for the user
     * @param companyId for which the company role is assigned to the user
     * @param userId of the user whose company role shall be removed
     */
    @Operation(
        summary = "Remove company role for the company from the user.",
        description = "The company role for the specified company is being removed from the user.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully removed company role."),
        ],
    )
    @DeleteMapping(
        produces = ["application/json"],
        value = ["/company-role-assignments/{role}/{companyId}/{userId}"],

    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun removeCompanyRole(
        @PathVariable("role") companyRole: CompanyRole,
        @PathVariable("companyId") companyId: UUID,
        @PathVariable("userId") userId: UUID,
    )

    /**
     * A method to check if a user has a certain company role for a specific company
     * @param companyRole to check for
     * @param companyId of the company for which the user might have the role
     * @param userId of the user
     */
    @Operation(
        summary = "Validate company role for company and user.",
        description = "Checks whether the company role for the company is assigned to the user.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "The specified user has the company role."),
            ApiResponse(
                responseCode = "404",
                description = "Either the specified company does not exist on Dataland or" +
                    " the user does not have the company role for it.",
            ),
        ],
    )
    @RequestMapping(
        method = [RequestMethod.HEAD],
        value = ["/company-role-assignments/{role}/{companyId}/{userId}"],
    )
    @PreAuthorize("hasRole('ROLE_ADMIN') or @SecurityUtilsService.isUserRequestingForOwnId(#userId)")
    fun hasUserCompanyRole(
        @PathVariable("role") companyRole: CompanyRole,
        @PathVariable("companyId") companyId: UUID,
        @PathVariable("userId") userId: UUID,
    )

    /**
     * A method to check if a user is company owner of any company
     * @param userId of the user
     */
    @Operation(
        summary = "Validate company ownership for user.",
        description = "Checks whether the user is a company owner.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "The specified user is a company owner."),
            ApiResponse(
                responseCode = "404",
                description = "The user is not a company owner.",
            ),
        ],
    )
    @RequestMapping(
        method = [RequestMethod.HEAD],
        value = ["/company-ownership/owner/{userId}"],
    )
    @PreAuthorize("hasRole('ROLE_ADMIN') or @SecurityUtilsService.isUserRequestingForOwnId(#userId)")
    fun hasUserCompanyOwnerRole(
        @PathVariable("userId") userId: UUID,
    )

    /**
     * A method to request company ownership for a specified company for the current user
     * @param companyId the ID of the company for which company ownership is requested
     */
    @Operation(
        summary = "Request company ownership for the company.",
        description = "Request company ownership for the company on Dataland.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully requested company ownership."),
        ],
    )
    @PostMapping(
        value = ["/company-ownership/{companyId}"],
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    fun postCompanyOwnershipRequest(
        @PathVariable("companyId") companyId: UUID,
        @RequestParam comment: String? = null,
    )

    /**
     * A method to verify if a company has at least one company owner
     * @param companyId of the company for which the information should be retrieved
     */
    @Operation(
        summary = "Validate existence of company ownership for the company.",
        description = "Validates if at least one company owner exists for the specified company",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "The specified company has company owner(s)."),
            ApiResponse(
                responseCode = "404",
                description = "The specified company has no company owner(s) yet",
            ),
        ],
    )
    @RequestMapping(
        method = [RequestMethod.HEAD],
        value = ["/company-ownership/{companyId}"],
    )
    fun hasCompanyAtLeastOneOwner(
        @PathVariable("companyId") companyId: UUID,
    )
}
