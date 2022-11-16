package org.dataland.datalandapikeymanager.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.dataland.datalandapikeymanager.model.ApiKey
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * Defines the restful dataland-backend API regarding company data.
 */
@RequestMapping("/api-keys")
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
}
