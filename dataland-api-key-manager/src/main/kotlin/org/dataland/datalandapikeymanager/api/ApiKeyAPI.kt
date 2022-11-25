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
 * Defines the restful api-key-manager API.
 */
interface ApiKeyAPI {

    @Operation(
        summary = "Request a new API key with expiration date and associated.",
        description = "Requests a new API key with expiration date associated for the logged in user."
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
    @PreAuthorize("hasRole('ROLE_USER')") /* TDO why?  (Emanuel asking)
    TDO PPU: welche Rolle man hat ist eigentlich egal
    TDO aber alle Rollen die man hat müssen dann auch mit dem API-Key assoziiert werden.
    TDO Emanuel: Aber warum Oauth zusätzlich? Ist das wegen der swagger UI Authorize Geschichte?
    TDO Florian: Scheint nicht notwendig zu sein. Das kam in der CompanyAPI vor, inwiefern wird das benutzt?
    */
    @SecurityRequirement(name = "default-bearer-auth")
    @SecurityRequirement(name = "default-oauth")
    /**
     * A method to generate a new API key
     * @param daysValid int determining how many days the generated API key can be used
     * @return new API key for the user
     */
    fun generateApiKey(@RequestParam(required = false) daysValid: Int? = null): ResponseEntity<ApiKeyAndMetaInfo>

    @Operation(
        summary = "Validate an API key.",
        description = "Check if an API key is valid."
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
     * @param apiKey holds the apiKey which needs to be validated
     * @return "true" if API key is valid, else "false"
     */

    fun validateApiKey(
        @RequestParam apiKey: String,
    ): ResponseEntity<ApiKeyMetaInfo>

    @Operation(
        summary = "Revoke an existing API key.",
        description = "Check if API key exists in storage for the requesting user and then revoke it."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "API key revokement process finished.")
        ]
    )
    @PostMapping(
        produces = ["application/json"],
        consumes = ["application/json"]
    )
    /**
     * A method to revoke the API key of the requesting user.
     * @return "true" if API key is valid, else "false"
     */

    // TDO: PreAuthorize überdenken
    // TDO Antwort von Emanuel:  Wieso sollten wir es überdenken? Wir brauchen die userId des
    // TDO revokenden Users, und es soll ja auch nur jeder User für sich revoken können.
    @PreAuthorize("hasRole('ROLE_USER')")
    @SecurityRequirement(name = "default-bearer-auth")
    @SecurityRequirement(name = "default-oauth")
    fun revokeApiKey(): ResponseEntity<RevokeApiKeyResponse>
}
