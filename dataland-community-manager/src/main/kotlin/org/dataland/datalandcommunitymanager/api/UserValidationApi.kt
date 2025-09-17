package org.dataland.datalandcommunitymanager.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.dataland.datalandbackendutils.model.KeycloakUserInfo
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.CompanyIdParameterRequired
import org.dataland.datalandcommunitymanager.model.EmailAddress
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import java.util.UUID

/**
 * Defines the community manager API regarding validation of user information
 */
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
interface UserValidationApi {
    /**
     * Post an email address for validation. It will be checked whether the email address belongs to
     * some registered Dataland user and, if so, basic information on the user will be returned. Only
     * Dataland admins as well as users which are owner or admin of at least one company can use this.
     */
    @Operation(
        summary = "Validate an email address and obtain user-related information from an email address.",
        description =
            "Based on an email address, learn if there is a Dataland user with that email address " +
                "and if so, obtain their Dataland user ID, first and last name.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully validated the posted email address."),
            ApiResponse(
                responseCode = "400",
                description = "The posted string does not have the format of an email address.",
                content = [Content(schema = Schema())],
            ),
            ApiResponse(
                responseCode = "403",
                description = "You do not have permission to query user information based on email addresses.",
                content = [Content(schema = Schema())],
            ),
            ApiResponse(
                responseCode = "404",
                description = "No Dataland user is registered under this email address.",
                content = [Content(schema = Schema())],
            ),
        ],
    )
    @PostMapping(
        value = ["/user-validation/email"],
        consumes = ["application/json"],
        produces = ["application/json"],
    )
    @PreAuthorize(
        "hasRole('ROLE_ADMIN') or @CompanyRolesManager.currentUserIsOwnerOrAdminOfAtLeastOneCompany()",
    )
    fun postEmailAddressValidation(
        @RequestBody emailAddress: EmailAddress,
    ): ResponseEntity<KeycloakUserInfo>

    /**
     * Get all users whose email matches the suffixes defined in the company's emailSuffix attribute.
     * @param companyId the company to check
     * @return list of users with emails matching the suffixes
     */
    @Operation(
        summary = "Get users by company email suffixes.",
        description = "Returns all users whose email address ends with any of the suffixes defined in the company's emailSuffix attribute.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved users with matching email suffixes.",
            ),
            ApiResponse(
                responseCode = "403",
                description = "You do not have the right to make this query.",
                content = [Content(array = ArraySchema())],
            ),
            ApiResponse(
                responseCode = "404",
                description = "The specified company does not exist or has no emailSuffix defined.",
                content = [Content(array = ArraySchema())],
            ),
        ],
    )
    @GetMapping(
        produces = ["application/json"],
        value = ["/company-role-assignments/by-email-suffix/{companyId}"],
    )
    @PreAuthorize(
        "hasRole('ROLE_ADMIN') or " +
            "@SecurityUtilsService.isUserOwnerOrMemberAdminOfTheCompany(#companyId)",
    )
    fun getUsersByCompanyEmailSuffix(
        @CompanyIdParameterRequired
        @PathVariable("companyId") companyId: UUID,
    ): ResponseEntity<List<KeycloakUserInfo>>
}
