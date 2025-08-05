package org.dataland.datalandcommunitymanager.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.dataland.datalandbackendutils.model.KeycloakUserInfo
import org.dataland.datalandcommunitymanager.model.EmailAddress
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

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
}
