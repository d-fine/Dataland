package org.dataland.datalandaccountingservice.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.dataland.datalandaccountingservice.model.TransactionDto
import org.dataland.datalandaccountingservice.model.TransactionPost
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import java.math.BigDecimal

/**
 * API interface for managing Dataland credits.
 */
@RequestMapping("/credits")
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
interface CreditsApi {
    /**
     * Get the current Dataland credits balance for a company.
     */
    @Operation(
        summary = "Get the current Dataland credits balance for a company.",
        description = "Retrieve the current balance of Dataland credits for the specified company.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved the credits balance."),
            ApiResponse(
                responseCode = "403",
                description = "Only Dataland admins and users with a role in the specified company may query its balance.",
                content = [Content(schema = Schema())],
            ),
            ApiResponse(
                responseCode = "404",
                description = "The specified company ID was not found.",
                content = [Content(schema = Schema())],
            ),
        ],
    )
    @GetMapping(
        value = ["/{companyId}/balance"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_ADMIN') or @AccountingAuthorizationService.hasUserSomeRoleInCompany(#companyId)")
    fun getBalance(
        @PathVariable("companyId") companyId: String,
    ): ResponseEntity<BigDecimal>

    /**
     * Post a credits transaction for a company.
     */
    @Operation(
        summary = "Post a credits transaction for a company.",
        description = "Add a (positive or negative) amount of Dataland credits to the balance of a company.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully posted the credits transaction."),
            ApiResponse(
                responseCode = "403",
                description = "Only Dataland admins may post transactions.",
                content = [Content(schema = Schema())],
            ),
            ApiResponse(
                responseCode = "404",
                description = "The specified company ID was not found.",
                content = [Content(schema = Schema())],
            ),
        ],
    )
    @PostMapping(
        value = ["/{companyId}/transaction"],
        consumes = ["application/json"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun postTransaction(
        @PathVariable("companyId") companyId: String,
        @RequestBody transactionPost: TransactionPost,
    ): ResponseEntity<TransactionDto<String>>
}
