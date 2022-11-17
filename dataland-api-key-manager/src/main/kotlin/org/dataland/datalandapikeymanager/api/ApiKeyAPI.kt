package org.dataland.datalandapikeymanager.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.dataland.datalandapikeymanager.model.ApiKey
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * Defines the restful api-key-manager API.
 */
interface ApiKeyAPI {

    @Operation(
        summary = "Request a new API key with expiration date and associated with a user.",
        description = "Requests a new API key with expiration date associated with a user."
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
    /**
     * A method to generate a new API key
     * @param username string used for
     * @param expiryDate string determining until when the generated API key can be used
     * @return new API key for the user
     */
    fun generateApiKey(
        @RequestParam username: String? = null,
        @RequestParam expiryDate: String? = null
    ): ResponseEntity<ApiKey>

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

    /* TODO / idea for later: Give more detailed message like "Api Key valid",
    "Api Key invalid. Reason: Not found", "Api Key invalid. Reason: Expired"
     */
    fun validateApiKey(
        @RequestParam apiKey: String? = null,
    ): ResponseEntity<Boolean>
}
