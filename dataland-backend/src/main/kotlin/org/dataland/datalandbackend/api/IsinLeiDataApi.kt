package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.dataland.datalandbackend.model.IsinLeiMappingData
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

/**
 * Defines the restful dataland-backend API regarding ISIN to LEI data mapping.
 */
@RequestMapping("/isinleimapping")
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
fun interface IsinLeiDataApi {
    /**
     * A method to update the ISIN-LEI mapping entirely
     * @param isinLeiMappingData ISIN-LEI mapping data
     * @return updated ISIN-LEI mapping data
     */
    @Operation(
        summary = "Update ISIN-LEI mapping entirely",
        description = "Replace all ISIN-LEI mappings with the given mappings.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully updated ISIN-LEI mapping."),
        ],
    )
    @PutMapping(
        value = ["/update"],
        consumes = ["application/json"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun putIsinLeiMapping(
        @Valid @RequestBody
        isinLeiMappingData: List<IsinLeiMappingData>,
    ): ResponseEntity<List<IsinLeiMappingData>>
}
