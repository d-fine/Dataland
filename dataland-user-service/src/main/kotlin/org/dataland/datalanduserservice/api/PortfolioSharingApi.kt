package org.dataland.datalanduserservice.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.UserServiceOpenApiDescriptionsAndExamples
import org.dataland.datalanduserservice.model.BasePortfolio
import org.dataland.datalanduserservice.model.BasePortfolioName
import org.dataland.datalanduserservice.model.PortfolioSharingPatch
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody

/**
 * --- API interface ---
 * Portfolio Sharing API interface
 */
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
interface PortfolioSharingApi {
    /**
     * Get all shared portfolios for the currently logged-in user.
     */
    @Operation(
        summary = "Get all shared portfolios for the currently logged-in user.",
        description = "All shared portfolios for the currently logged-in user are retrieved.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved all shared portfolios."),
        ],
    )
    @GetMapping(
        value = ["/portfolios/shared"],
        produces = ["application/json"],
    )
    @PreAuthorize(
        "hasRole('ROLE_USER')",
    )
    fun getAllSharedPortfoliosForCurrentUser(): ResponseEntity<List<BasePortfolio>>

    /**
     * Patch the portfolio sharing of a portfolio.
     */
    @Operation(
        summary = "Patch the portfolio sharing of a portfolio.",
        description = "Updates the the list of user IDs with whom an existing portfolio is shared.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully updated sharing."),
            ApiResponse(responseCode = "403", description = "Only Dataland admins and portfolio owners can modify portfolio sharing."),
        ],
    )
    @PatchMapping(
        value = ["/portfolios/{portfolioId}/sharing"],
        consumes = ["application/json"],
        produces = ["application/json"],
    )
    @PreAuthorize(
        "hasRole('ROLE_ADMIN') or (hasRole('ROLE_USER') " +
            "and @PortfolioRightsUtilsComponent.isUserPortfolioOwner(#portfolioId))",
    )
    fun patchSharing(
        @Parameter(
            description = UserServiceOpenApiDescriptionsAndExamples.PORTFOLIO_ID_DESCRIPTION,
            example = UserServiceOpenApiDescriptionsAndExamples.PORTFOLIO_ID_EXAMPLE,
            required = true,
        )
        @PathVariable("portfolioId") portfolioId: String,
        @Valid @RequestBody portfolioSharingPatch: PortfolioSharingPatch,
    ): ResponseEntity<BasePortfolio>

    /**
     * Delete the sharing of a portfolio.
     */
    @Operation(
        summary = "Delete the sharing of a portfolio for the currently logged-in user.",
        description = "The currently logged-in user is removed from the list of users with whom the portfolio is shared.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Successfully deleted sharing for the current user."),
            ApiResponse(responseCode = "403", description = "You can only remove yourself from portfolios that are shared with you."),
        ],
    )
    @DeleteMapping(
        value = ["/portfolios/shared/{portfolioId}"],
    )
    @PreAuthorize(
        "hasRole('ROLE_ADMIN') or (hasRole('ROLE_USER') " +
            "and @PortfolioRightsUtilsComponent.isPortfolioSharedWithUser(authentication.userId, #portfolioId))",
    )
    fun deleteCurrentUserFromSharing(
        @Parameter(
            description = UserServiceOpenApiDescriptionsAndExamples.PORTFOLIO_ID_DESCRIPTION,
            example = UserServiceOpenApiDescriptionsAndExamples.PORTFOLIO_ID_EXAMPLE,
            required = true,
        )
        @PathVariable("portfolioId") portfolioId: String,
    ): ResponseEntity<Unit>

    /**
     * Get all shared portfolio names for currently logged-in user.
     */
    @Operation(
        summary = "Get all shared portfolio names for the currently logged-in user.",
        description = "All shared portfolio names for the currently logged-in user are retrieved.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved all shared portfolio names."),
        ],
    )
    @GetMapping(
        value = ["/portfolios/shared/names"],
        produces = ["application/json"],
    )
    @PreAuthorize(
        "hasRole('ROLE_USER')",
    )
    fun getAllSharedPortfolioNamesForCurrentUser(): ResponseEntity<List<BasePortfolioName>>
}
