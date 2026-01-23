package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.dataland.datalandbackend.model.enums.export.ExportJobProgressState
import org.springframework.core.io.InputStreamResource
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

/**
 * Defines the restful dataland-backend API regarding data exchange
 */

@RequestMapping("/export")
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
interface DataExportApi {
    /**
     * A method to get state an export job by its ID.
     */
    @Operation(
        summary = "Get information about export job being done.",
        description =
            "Check state of export job.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully exported datasets."),
            ApiResponse(
                responseCode = "204",
                description = "No data for download available.",
                content = [Content(mediaType = "")],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Export job Id could not be found.",
                content = [Content(mediaType = "")],
            ),
        ],
    )
    @GetMapping(
        value = ["/state/{exportJobId}"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    fun getExportJobState(
        @PathVariable exportJobId: String,
    ): ResponseEntity<ExportJobProgressState>

    /**
     * A method to download the CompanyAssociatedData by its export job ID.
     */
    @Operation(
        summary = "Export data exportJobId provided.",
        description =
            "Given the exportJobId download the corresponding file.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully exported datasets."),
            ApiResponse(
                responseCode = "204",
                description = "No data for download available.",
                content = [Content(mediaType = "")],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Company Id could not be found.",
                content = [Content(mediaType = "")],
            ),
        ],
    )
    @GetMapping(
        value = ["/download/{exportJobId}"],
        produces = ["application/octet-stream"],
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    fun exportCompanyAssociatedDataById(
        @PathVariable exportJobId: String,
    ): ResponseEntity<InputStreamResource>
}
