package org.dataland.datalandinternalstorage.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping

/**
 * Defines the restful api-key-manager API.
 */
interface StorageAPI {

    @Operation(
        summary = "Request data by id.",
        description = "Requests data by id."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved data.")
        ]
    )
    @GetMapping(
        value = ["/get"]
    )
    fun selectDataById(dataId: String, correlationId: String?): ResponseEntity<String>

    @Operation(
        summary = "Upload data with id.",
        description = "Upload data with id."
    )
    @ApiResponses(
        value = [ApiResponse(responseCode = "200", description = "Successfully retrieved data.")]
    )
    @PostMapping(
        value = ["/post"]
    )
    fun insertData(correlationId: String?, body: String?): ResponseEntity<String>

//    override fun checkHealth(): ResponseEntity<CheckHealthResponse> {
//        return ResponseEntity.ok(CheckHealthResponse("I am alive!"))
//    }
}
