package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.dataland.datalandbackend.model.IsinLeiMappingData
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * Defines the restful dataland-backend API regarding ISIN to LEI data mapping.
 */
@RequestMapping("/isinleimapping")
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
interface IsinLeiDataApi {
    /**
     * A method to update the ISIN-LEI mapping entirely
     * @param isinLeiMappingData ISIN-LEI mapping data
     * @return updated ISIN-LEI mapping data
     */
    @Operation(
        summary = "Post a new ISIN-LEI mapping.",
        description = "Replaces all ISIN-LEI mappings with the given mappings.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully updated ISIN-LEI mapping."),
        ],
    )
    @PostMapping(
        value = ["/"],
        consumes = ["application/json"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun postIsinLeiMapping(
        @Valid @RequestBody
        isinLeiMappingData: List<IsinLeiMappingData>,
    ): ResponseEntity<Map<String?, String?>?>

    /**
     * A method to retrieve all ISINs registered for a given [lei]. If no ISINs are registered for the LEI, an empty list is returned.
     * @param lei the LEI identifier to search for ISINs
     * @return a list of ISINs associated with the given LEI
     */
    @Operation(
        summary = "Retrieves all ISINs for a given LEI",
        description = "All ISINs associated to the given LEI are returned. Returns empty list if no ISINs are registered for the LEI.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved all ISINs associated with the provided LEI."),
        ],
    )
    @GetMapping(
        value = ["/isins"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    fun getIsinsByLei(
        @Parameter(
            name = "lei",
            description = "The LEI for which the associated ISINs are requested.",
            example = GeneralOpenApiDescriptionsAndExamples.GENERAL_LEI_EXAMPLE,
            required = true,
        )
        @RequestParam("lei") lei: String,
    ): ResponseEntity<List<String>>
}
