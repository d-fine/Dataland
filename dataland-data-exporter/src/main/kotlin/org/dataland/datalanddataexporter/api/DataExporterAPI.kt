package org.dataland.datalanddataexporter.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping


/**
 * Defines the api-key-manager API.
 */
interface DataExporterAPI {

    /**
     * A method to test the API.
     * @return simple string response.
     */
    @Operation(
        summary = "Dummy Endpoint.",
        description = "Dummy endpoint to test the API.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved message."),
        ],
    )
    @GetMapping(
        value = ["/test"],
        produces = ["application/json"],
    )
    fun respond(): ResponseEntity<String>
}
