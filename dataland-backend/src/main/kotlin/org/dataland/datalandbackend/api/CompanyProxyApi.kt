package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.dataland.datalandbackend.model.proxies.CompanyProxyString
import org.dataland.datalandbackend.model.proxies.StoredCompanyProxy
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.validator.CompanyExists
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * Defines the REST API for managing proxies between companies.
 */
@RequestMapping("/company-proxies")
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
interface CompanyProxyApi {
    // TODO: Add validation for the input. Check if companyIds are companyIds, frameworks are frameworks and reportingPeriods are reportingPeriods.

    /**
     * Creates a proxy rule for a given (proxiedCompanyId, proxyCompanyId) pair.
     *
     * The submitted resource defines which data of one company may be substituted by
     * the corresponding data of another company.
     *
     * If the framework or reportingPeriod are null, the proxy applies to all frameworks
     * or all reporting periods respectively.
     */
    @Operation(
        summary = "Create a proxy.",
        description =
            "Creates a proxy describing which data of one company may be substituted " +
                "by data of another company. If framework or reportingPeriod are null, " +
                "the proxy applies to all frameworks or all reporting periods respectively.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully stored proxy.",
            ),
            ApiResponse(
                responseCode = "403",
                description = "Only Dataland admins may create company proxies.",
                content = [Content(array = ArraySchema())],
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid input.",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    @PostMapping(
        consumes = ["application/json"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun postCompanyProxy(
        @Valid @RequestBody
        companyProxy: CompanyProxyString,
    ): ResponseEntity<StoredCompanyProxy>

    // TODO: No work has been put into any get feature. Should be similar to the /data-points endpoint in the QA-Controller.

    @Operation(
        summary = "Get the company proxy for a given id.",
        description = "Retrieve the company proxy for the given proxy id. ",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved proxy.",
            ),
            ApiResponse(
                responseCode = "403",
                description = "Only Dataland admins may query company proxies.",
                content = [Content(array = ArraySchema())],
            ),
            ApiResponse(
                responseCode = "404",
                description = "No proxy found for the specified proxy id.",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    @GetMapping(
        value = ["/company-proxy/{proxyId}"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun getCompanyProxyById(
        @Parameter(
            description = GeneralOpenApiDescriptionsAndExamples.PROXY_ID_DESCRIPTION,
            example = GeneralOpenApiDescriptionsAndExamples.PROXY_ID_EXAMPLE,
            required = true,
        )
        @PathVariable proxyId: String,
    ): ResponseEntity<StoredCompanyProxy>

    /**
     * Searches for company proxies matching the given filters.
     */
    @Operation(
        summary = "Search company proxies.",
        description = "Searches for company proxies matching the given filters.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved proxies."),
        ],
    )
    @GetMapping(
        value = ["/company-proxy"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun searchCompanyProxies(
        @RequestParam
        @Parameter(
            description = GeneralOpenApiDescriptionsAndExamples.PROXIED_COMPANY_ID_DESCRIPTION,
            example = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
            required = false,
        )
        @CompanyExists
        proxiedCompanyId: String?,
        @RequestParam
        @Parameter(
            description = GeneralOpenApiDescriptionsAndExamples.PROXY_COMPANY_ID_DESCRIPTION,
            example = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
            required = false,
        )
        @CompanyExists
        proxyCompanyId: String?,
        @RequestParam
        @Parameter(
            description = GeneralOpenApiDescriptionsAndExamples.DATA_TYPE_DESCRIPTION,
            example = GeneralOpenApiDescriptionsAndExamples.DATA_TYPES_FRAMEWORK_EXAMPLE,
            required = false,
        )
        frameworks: Set<String>?,
        @RequestParam
        @Parameter(
            description = GeneralOpenApiDescriptionsAndExamples.GENERAL_REPORTING_PERIODS_DESCRIPTION,
            example = GeneralOpenApiDescriptionsAndExamples.GENERAL_REPORTING_PERIODS_EXAMPLE,
            required = false,
        )
        reportingPeriods: Set<String>?,
        @RequestParam(defaultValue = "10")
        @Parameter(
            description = GeneralOpenApiDescriptionsAndExamples.CHUNK_SIZE_DESCRIPTION,
            required = false,
        )
        chunkSize: Int,
        @RequestParam(defaultValue = "0")
        @Parameter(
            description = GeneralOpenApiDescriptionsAndExamples.CHUNK_INDEX_DESCRIPTION,
            required = false,
        )
        chunkIndex: Int,
    ): ResponseEntity<List<StoredCompanyProxy>>

    /**
     * Deletes a single proxy relation by its technical ID.
     */
    @Operation(
        summary = "Delete a proxy relation by technical ID.",
        description =
            "Deletes a single proxy relation identified by its technical ID and returns the deleted rule.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully deleted proxy rule.",
            ),
            ApiResponse(
                responseCode = "404",
                description = "No proxy relation found for the specified technical ID.",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    @DeleteMapping(
        value = ["/company-proxies/{proxyId}"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun deleteCompanyProxy(
        @Parameter(
            name = "proxyId",
            description = "The proxy ID of the proxy rule entry.",
            required = true,
        )
        @PathVariable proxyId: String,
    ): ResponseEntity<StoredCompanyProxy>

    /**
     * Replace the proxy for a given (unique) company proxy-ID.
     */
    @Operation(
        summary = "Replace proxy entry for a company pair.",
        description =
            "Replaces all proxy rules defined for the given proxied company and proxy company." +
                " If the lists or reportingPeriods are empty or null, the proxy applies to all of them.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully replaced proxy.",
            ),
            ApiResponse(
                responseCode = "403",
                description = "Only Dataland admins may modify company proxies.",
                content = [Content(array = ArraySchema())],
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid input.",
                content = [Content(mediaType = "application/json")],
            ),
            ApiResponse(
                responseCode = "404",
                description = "No proxy found for the specified company pair.",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    @PutMapping(
        value = ["/company-proxy/{proxyId}"],
        consumes = ["application/json"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun putCompanyProxy(
        @Parameter(
            description = GeneralOpenApiDescriptionsAndExamples.PROXY_ID_DESCRIPTION,
            example = GeneralOpenApiDescriptionsAndExamples.PROXY_ID_EXAMPLE,
            required = true,
        )
        @PathVariable proxyId: String,
        @Valid
        @RequestBody
        companyProxy: CompanyProxyString,
    ): ResponseEntity<StoredCompanyProxy>
}
