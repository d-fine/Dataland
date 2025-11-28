package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.dataland.datalandbackend.model.proxies.CompanyProxy
import org.dataland.datalandbackend.model.proxies.CompanyProxyRelationResponse
import org.dataland.datalandbackend.model.proxies.CompanyProxyRequest
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.CompanyIdParameterRequired
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
import java.util.UUID

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
        companyProxy: CompanyProxyRequest,
    ): ResponseEntity<CompanyProxyRelationResponse>

    // TODO: No work has been put into any get feature. Should be similar to the /data-points endpoint in the QA-Controller.

    /**
     * Retrieves the proxy for a given (proxiedCompanyId, proxyCompanyId) pair.
     *
     * The returned object merges all stored proxy rows into a single DTO.
     * Empty lists indicate that proxying applies to all frameworks or all reporting periods.
     */
    @Operation(
        summary = "Retrieve proxy  for a company pair.",
        description =
            "Returns the proxy defined for the given proxied company and proxy company. ",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved proxy.",
            ),
            ApiResponse(
                responseCode = "403",
                description = "Only Dataland admins may query company rights.",
                content = [Content(array = ArraySchema())],
            ),
            ApiResponse(
                responseCode = "404",
                description = "No proxy found for the specified company pair.",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    @GetMapping(
        value = ["/company-proxy"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun getCompanyProxy(
        @CompanyIdParameterRequired
        @Parameter(
            name = "proxiedCompanyId",
            description = "The company whose data may be proxied.",
            required = true,
        )
        @CompanyExists
        @RequestParam proxiedCompanyId: String,
    ): ResponseEntity<List<CompanyProxy>>

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
        value = ["/company-proxies"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    fun deleteCompanyProxy(
        @Parameter(
            name = "proxyId",
            description = "The proxy ID of the proxy rule entry.",
            required = true,
        )
        @RequestParam proxyId: String,
    ): ResponseEntity<CompanyProxyRelationResponse>

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
        value = ["/company-proxy"],
        consumes = ["application/json"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun putCompanyProxy(
        @Parameter(
            name = "proxyId",
            description = GeneralOpenApiDescriptionsAndExamples.PROXY_ID_DESCRIPTION,
            example = GeneralOpenApiDescriptionsAndExamples.GENERAL_UUID_EXAMPLE,
            required = true,
        )
        @PathVariable("proxyId")
        proxyId: UUID,
        @Valid
        @RequestBody
        companyProxy: CompanyProxy,
    ): ResponseEntity<CompanyProxy>
}
