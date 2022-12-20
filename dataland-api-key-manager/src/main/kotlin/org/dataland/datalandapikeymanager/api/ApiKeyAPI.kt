package org.dataland.datalandapikeymanager.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.dataland.datalandapikeymanager.model.ApiKeyAndMetaInfo
import org.dataland.datalandapikeymanager.model.ApiKeyMetaInfo
import org.dataland.datalandapikeymanager.model.RevokeApiKeyResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * Defines the api-key-manager API.
 */
interface ApiKeyAPI {

    @Operation(
        summary = "Generate a new API key.",
        description = "Generates and persists a new API key for the requesting user with an expiry date based on " +
            "the number of valid days in the request param."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved new api key.")
        ]
    )
    @GetMapping(
        value = ["/generateApiKey"],
        produces = ["application/json"]
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    @SecurityRequirement(name = "default-bearer-auth")
    @SecurityRequirement(name = "default-oauth")
    /**
     * A method to generate a new API key
     * @param daysValid defines how many days the generated API key can be used, a null value results in an
     * infinite validity
     * @return new API key for the user together with meta info associated with that API key
     */
    fun generateApiKey(
        @RequestParam(required = false) daysValid: Int? = null
    ): ResponseEntity<ApiKeyAndMetaInfo>

    @Operation(
        summary = "Get API key meta info of a specific user.",
        description = "Gets meta info about the API key status of a user based on the Keycloak user ID."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved API key status for the user ID.")
        ]
    )
    @GetMapping(
        value = ["/getApiKeyMetaInfoForUser"],
        produces = ["application/json"]
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    @SecurityRequirement(name = "default-bearer-auth")
    @SecurityRequirement(name = "default-oauth")
    /** A method to get meta information about the API key status of a specific user, like if that user
     * even has an API key registered, and what the expiry date of that (potential) API key is.
     * Information about the user is derived from the Bearer token of the request.
     * This method is needed by the Frontend to display the API key status to a logged in user.
     * @return API key meta info which includes all the required info for the API key status
     */
    fun getApiKeyMetaInfoForUser(): ResponseEntity<ApiKeyMetaInfo>

    @Operation(
        summary = "Validate an API key.",
        description = "Checks if an API key is valid and returns the validation results together with its meta info."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "API key validation process finished.")
        ]
    )
    @GetMapping(
        value = ["/validateApiKey"],
        produces = ["application/json"]
    )
    /**
     * A method to validate an API key
     * @param apiKey holds the API key which needs to be validated as string
     * @return API key meta info which also includes the result of the validation process
     */

    fun validateApiKey(@RequestParam apiKey: String,): ResponseEntity<ApiKeyMetaInfo>

    @Operation(
        summary = "Revoke an existing API key.",
        description = "Checks if API key exists in storage for the requesting user and revokes it. If there is no " +
            "API key registered for the user, this is reported in the response."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "API key revokement process finished.")
        ]
    )
    @PostMapping(
        value = ["/revokeApiKey"],
        produces = ["application/json"]
    )
    /**
     * A method to revoke the API key of the requesting user.
     * @return a response model which informs about the success of the revokement process together with a message.
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @SecurityRequirement(name = "default-bearer-auth")
    @SecurityRequirement(name = "default-oauth")
    fun revokeApiKey(): ResponseEntity<RevokeApiKeyResponse>
}
