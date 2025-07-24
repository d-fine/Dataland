package org.dataland.datalandcommunitymanager.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.web.bind.annotation.GetMapping

/**
 * Defines the community manager API regarding Keycloak user information
 */
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
interface UserApi {
    /**
     * Thoughtful comment.
     */
    @Operation(
        summary = "Obtain user-related information from an email address.",
        description = "Based on an email address, return Dataland user ID, first and last name."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved user information."),
            ApiResponse(
                responseCode = "403",
                description = "You do not have permission to query user information based on email addresses."
            ),
            ApiResponse(responseCode = "404", description = "No Dataland user is registered under this email address.")
        ],
    )
    @GetMapping(
        value = "user-information/{email}",
        produces = ["application/json"],
    )

}