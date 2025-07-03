package org.dataland.datalanduserservice.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.dataland.datalandbackendutils.utils.BackendOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.UsersOpenApiDescriptionsAndExamples
import org.dataland.datalanduserservice.model.BasePortfolio
import org.dataland.datalanduserservice.model.BasePortfolioName
import org.dataland.datalanduserservice.model.EnrichedPortfolio
import org.dataland.datalanduserservice.model.PortfolioMonitoringPatch
import org.dataland.datalanduserservice.model.PortfolioUpload
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

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
            ApiResponse(responseCode = "200", description = "Successfully retrieved portfolio."),
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
        @Parameter(
            description = UsersOpenApiDescriptionsAndExamples.PORTFOLIO_ID_DESCRIPTION,
            example = UsersOpenApiDescriptionsAndExamples.PORTFOLIO_ID_EXAMPLE,
            required = true,
        )
        @PathVariable("portfolioId") portfolioId: String,
    ): ResponseEntity<BasePortfolio>

    /**
     * Get all portfolios for a given user. This is an admin-only endpoint.
     */
    @Operation(
        summary = "Get portfolios by userId.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved portfolios."),
        ],
    )
    @GetMapping(
        value = ["/portfolios/users/{userId}"],
    )
    @PreAuthorize(
        "hasRole('ROLE_ADMIN')",
    )
    fun getPortfoliosForUser(
        @Parameter(
            description = UsersOpenApiDescriptionsAndExamples.PORTFOLIO_USER_ID_DESCRIPTION,
            example = UsersOpenApiDescriptionsAndExamples.PORTFOLIO_USER_ID_EXAMPLE,
            required = true,
        )
        @PathVariable("userId") userId: String,
    ): ResponseEntity<List<BasePortfolio>>

    /**
     * Get a paginated list of all portfolios that exist on Dataland. This is an admin-only endpoint.
     */
    @Operation(
        summary = "Get a segment of all portfolios for the given chunk size and index.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved the requested chunk of portfolios."),
        ],
    )
    @GetMapping(
        value = ["/portfolios/all"],
    )
    @PreAuthorize(
        "hasRole('ROLE_ADMIN')",
    )
    fun getAllPortfolios(
        @RequestParam(defaultValue = "100")
        @Parameter(
            description = BackendOpenApiDescriptionsAndExamples.CHUNK_SIZE_DESCRIPTION,
            required = false,
        )
        chunkSize: Int,
        @RequestParam(defaultValue = "0")
        @Parameter(
            description = BackendOpenApiDescriptionsAndExamples.CHUNK_INDEX_DESCRIPTION,
            required = false,
        )
        chunkIndex: Int,
    ): ResponseEntity<List<BasePortfolio>>

    /**
     * Post a new portfolio.
     */
    @Operation(
        summary = "Post a new portfolio.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Successfully created a new portfolio."),
            ApiResponse(responseCode = "403", description = "Only premium users can activate portfolio monitoring."),
        ],
    )
    @PostMapping(
        value = ["/portfolios/"],
        consumes = ["application/json"],
        produces = ["application/json"],
    )
    @PreAuthorize(
        "(hasRole('ROLE_USER') and !#portfolioUpload.isMonitored) or hasRole('ROLE_PREMIUM_USER')",
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
            ApiResponse(responseCode = "403", description = "Only premium users can activate portfolio monitoring."),
        ],
    )
    @PutMapping(
        value = ["/portfolios/{portfolioId}/"],
        consumes = ["application/json"],
        produces = ["application/json"],
    )
    @PreAuthorize(
        "(hasRole('ROLE_USER') and !#portfolioUpload.isMonitored) or hasRole('ROLE_PREMIUM_USER')",
    )
    fun replacePortfolio(
        @Parameter(
            description = UsersOpenApiDescriptionsAndExamples.PORTFOLIO_ID_DESCRIPTION,
            example = UsersOpenApiDescriptionsAndExamples.PORTFOLIO_ID_EXAMPLE,
            required = true,
        )
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
        @Parameter(
            description = UsersOpenApiDescriptionsAndExamples.PORTFOLIO_ID_DESCRIPTION,
            example = UsersOpenApiDescriptionsAndExamples.PORTFOLIO_ID_EXAMPLE,
            required = true,
        )
        @PathVariable(name = "portfolioId") portfolioId: String,
    ): ResponseEntity<Unit>

    /**
     * Get all portfolio names for currently logged-in user.
     */
    @Operation(
        summary = "Get all portfolio names for the currently logged-in user.",
        description = "All portfolio names for the currently logged-in user are retrieved.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved all portfolio names."),
        ],
    )
    @GetMapping(
        value = ["/portfolios/names"],
        produces = ["application/json"],
    )
    @PreAuthorize(
        "hasRole('ROLE_USER')",
    )
    fun getAllPortfolioNamesForCurrentUser(): ResponseEntity<List<BasePortfolioName>>

    /**
     * Get enriched portfolio by portfolioId.
     */
    @Operation(
        summary = "Get enriched portfolio by portfolioId.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved portfolios."),
        ],
    )
    @GetMapping(
        value = ["/portfolios/{portfolioId}/enriched-portfolio"],
        produces = ["application/json"],
    )
    @PreAuthorize(
        "hasRole('ROLE_USER')",
    )
    fun getEnrichedPortfolio(
        @Parameter(
            description = UsersOpenApiDescriptionsAndExamples.PORTFOLIO_ID_DESCRIPTION,
            example = UsersOpenApiDescriptionsAndExamples.PORTFOLIO_ID_EXAMPLE,
            required = true,
        )
        @PathVariable("portfolioId") portfolioId: String,
    ): ResponseEntity<EnrichedPortfolio>

    /**
     * Patches the monitoring of an existing portfolio.
     */
    @Operation(
        summary = "Patches the monitoring status of a portfolio.",
        description = "Updates the monitoring-related fields of an existing portfolio.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully updated monitoring."),
            ApiResponse(responseCode = "403", description = "Only premium users can activate portfolio monitoring."),
        ],
    )
    @PatchMapping(
        value = ["/portfolios/{portfolioId}/monitoring"],
        consumes = ["application/json"],
        produces = ["application/json"],
    )
    @PreAuthorize("(hasRole('ROLE_USER') and !#portfolioMonitoringPatch.isMonitored) or hasRole('ROLE_PREMIUM_USER')")
    fun patchMonitoring(
        @Parameter(
            description = UsersOpenApiDescriptionsAndExamples.PORTFOLIO_ID_DESCRIPTION,
            example = UsersOpenApiDescriptionsAndExamples.PORTFOLIO_ID_EXAMPLE,
            required = true,
        )
        @PathVariable("portfolioId") portfolioId: String,
        @Valid @RequestBody portfolioMonitoringPatch: PortfolioMonitoringPatch,
    ): ResponseEntity<BasePortfolio>
}
