package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable


@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
interface TokenValidityApi {
    /**
     * Returns 200 if called with a valid token
     */
    @Operation(
            summary = "Validates if a token is valid",
            description = "Validates if a token is valid",
            )
    @ApiResponses(
            value = [
                ApiResponse(responseCode = "200"),
            ],
    )
    @GetMapping(
            value = ["/token"],
            produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    fun validateToken()
}