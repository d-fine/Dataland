package org.dataland.datalanduserservice.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

/**
 * Statistics API for reviewer-only endpoints.
 */
@RequestMapping("/subscribed-companies")
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
interface SubscribedCompaniesApi {
    /**
     * Returns the persistent human log of companyIDs whose fiscal year end or sector is still missing.
     */
    @Operation(
        summary = "Get companyIDs with incomplete company information",
        description =
            "Returns the persistent log of companyIDs whose fiscal year end or sector could not be completed" +
                " automatically. Companies that are complete by now are removed from the log before responding.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "List of companyIDs with incomplete company information.",
                content = [
                    Content(
                        array =
                            ArraySchema(
                                arraySchema =
                                    Schema(
                                        description = GeneralOpenApiDescriptionsAndExamples.COMPANY_IDS_DESCRIPTION,
                                        example = GeneralOpenApiDescriptionsAndExamples.COMPANY_IDS_EXAMPLE,
                                    ),
                                schema = Schema(type = "string"),
                            ),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "403", description = "Access denied. Only reviewers can access this endpoint.",
                content = [Content(array = ArraySchema())],
            ),
        ],
    )
    @PreAuthorize("hasRole('ROLE_REVIEWER')")
    @GetMapping(
        value = ["/incomplete-information"],
        produces = ["application/json"],
    )
    fun getCompaniesWithIncompleteInformation(): ResponseEntity<Set<String>>

    /**
     * Adds a company to the persistent human log of companies with incomplete information. Idempotent.
     */
    @Operation(
        summary = "Log a company with incomplete company information",
        description =
            "Adds the company to the persistent log of companies whose fiscal year end or sector could not be" +
                " completed automatically. Does nothing if the company is already logged.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "The company is in the log."),
            ApiResponse(
                responseCode = "403", description = "Access denied.",
                content = [Content(array = ArraySchema())],
            ),
            ApiResponse(
                responseCode = "404", description = "The company does not exist.",
                content = [Content(array = ArraySchema())],
            ),
        ],
    )
    @PreAuthorize("hasRole('ROLE_REVIEWER')")
    @PostMapping(value = ["/incomplete-information/{companyId}"])
    fun postCompanyWithIncompleteInformation(
        @PathVariable companyId: String,
    ): ResponseEntity<Unit>
}
