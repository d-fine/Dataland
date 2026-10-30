package org.dataland.datalandproxyfinder.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.dataland.datalandbackendutils.model.proxyfinder.ProxySuggestion
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

/**
 * Internal API of the proxy finder.
 */
@RequestMapping("/proxy-suggestions")
@SecurityRequirement(name = "default-bearer-auth")
interface ProxySuggestionApi {
    /**
     * Searches a proxy company for the given company.
     */
    @Operation(
        summary = "Find a proxy company for a company.",
        description = "Searches a proxy company for the specified company and returns a validated proxy suggestion.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully found a proxy suggestion."),
            ApiResponse(
                responseCode = "403",
                description = "Only Dataland admins may search proxy companies.",
                content = [Content(schema = Schema())],
            ),
            ApiResponse(
                responseCode = "404",
                description = "The specified company does not exist on Dataland.",
                content = [Content(schema = Schema())],
            ),
            ApiResponse(
                responseCode = "422",
                description =
                    "The proxy suggestion is invalid, e.g. the suggested company does not exist on Dataland " +
                        "or is the company itself.",
                content = [Content(schema = Schema())],
            ),
        ],
    )
    @PostMapping(
        value = ["/{companyId}"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun findProxySuggestion(
        @Parameter(
            description = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_DESCRIPTION,
            example = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
            required = true,
        )
        @PathVariable("companyId") companyId: String,
    ): ResponseEntity<ProxySuggestion>
}
