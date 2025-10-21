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
import org.springframework.web.bind.annotation.RequestMapping

/**
 * Statistics API for reviewer-only endpoints.
 */
@RequestMapping("/subscribed-companies")
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
interface SubscribedCompaniesApi {
    /**
     * Returns the temporary list of companyIDs with incomplete FYE information.
     *
     * The return value is determined by a nightly batch job and is constant throughout the day.
     */
    @Operation(
        summary = "Get companyIDs with incomplete FYE information",
        description =
            "Returns the temporary list of companyIDs with incomplete FYE information." +
                " The return value is determined by a nightly batch job and is constant throughout the day.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "List of companyIDs with incomplete FYE information.",
                content = [
                    Content(
                        array =
                            ArraySchema(
                                items =
                                    Schema(
                                        type = "string",
                                        description = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_DESCRIPTION,
                                        example = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
                                    ),
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
        value = ["/incomplete-fye-information"],
        produces = ["application/json"],
    )
    fun getCompaniesWithIncompleteFyeInformation(): ResponseEntity<Set<String>>
}
