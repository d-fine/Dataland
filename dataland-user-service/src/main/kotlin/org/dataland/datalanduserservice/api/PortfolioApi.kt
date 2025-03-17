package org.dataland.datalanduserservice.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.dataland.datalanduserservice.model.BasePortfolio
import org.dataland.datalanduserservice.model.PortfolioUpload
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody

/**
 * Defines the portfolio API for Dataland users to manage their portfolios.
 */
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
interface PortfolioApi {
    /**
     * Get all portfolios for currently logged-in user.
     */
    @Operation(
        summary = "Get all portfolios for the currently logged-in user.",
        description = "All portfolios for the currently logged-in user are retrieved.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved all portfolios."),
        ],
    )
    @GetMapping(
        value = ["/portfolios/"],
        produces = ["application/json"],
    )
    @PreAuthorize(
        "hasRole('ROLE_USER')",
    )
    fun getAllPortfoliosForCurrentUser(): ResponseEntity<List<BasePortfolio>>

    /**
     * Get portfolio by portfolioId.
     */
    @Operation(
        summary = "Get portfolio by portfolioId.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved portfolios."),
        ],
    )
    @GetMapping(
        value = ["/portfolios/{portfolioId}/"],
        produces = ["application/json"],
    )
    @PreAuthorize(
        "hasRole('ROLE_USER')",
    )
    fun getPortfolio(
        @PathVariable("portfolioId") portfolioId: String,
    ): ResponseEntity<BasePortfolio>

    /**
     * Post a new portfolio.
     */
    @Operation(
        summary = "Post a new portfolio.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Successfully created a new portfolio."),
        ],
    )
    @PostMapping(
        value = ["/portfolios/"],
        consumes = ["application/json"],
        produces = ["application/json"],
    )
    @PreAuthorize(
        "hasRole('ROLE_USER')",
    )
    fun createPortfolio(
        @Valid @RequestBody(required = true) portfolioUpload: PortfolioUpload,
    ): ResponseEntity<BasePortfolio>

    /**
     * Replace an existing portfolio.
     */
    @Operation(
        summary = "Replace an existing portfolio.",
        description = "Replace the existing portfolio with given portfolioId entirely",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully replaced existing portfolio."),
        ],
    )
    @PutMapping(
        value = ["/portfolios/{portfolioId}/"],
        consumes = ["application/json"],
        produces = ["application/json"],
    )
    @PreAuthorize(
        "hasRole('ROLE_USER')",
    )
    fun replacePortfolio(
        @PathVariable(name = "portfolioId") portfolioId: String,
        @Valid @RequestBody(required = true) portfolioUpload: PortfolioUpload,
    ): ResponseEntity<BasePortfolio>

    /**
     * Delete an existing portfolio.
     */
    @Operation(
        summary = "Delete an existing portfolio.",
        description = "Delete the portfolio with given portfolioId entirely",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Successfully deleted existing portfolio."),
        ],
    )
    @DeleteMapping(
        produces = ["application/json"],
        value = ["/portfolios/{portfolioId}/"],
    )
    @PreAuthorize(
        "hasRole('ROLE_USER')",
    )
    fun deletePortfolio(
        @PathVariable(name = "portfolioId") portfolioId: String,
    ): ResponseEntity<Unit>
}
