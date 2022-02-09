package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod


@RequestMapping("/")
interface HealthAPI {
    @Operation(summary = "Check if the API is responsive.", description = "Returns 200 if successful.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Service is up and running."),
            ApiResponse(responseCode = "404", description = "Service does not respond.")
        ]
    )
    @RequestMapping(
        method = [RequestMethod.GET],
        value = ["/health"],
        produces = ["text/plain"]
    )
    fun getHealth(): ResponseEntity<String> {
        return ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
    }
}