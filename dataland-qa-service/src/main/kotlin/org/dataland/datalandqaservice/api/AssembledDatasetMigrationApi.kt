package org.dataland.datalandqaservice.org.dataland.datalandqaservice.api

import com.fasterxml.jackson.databind.JsonNode
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReportMetaInformation
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

/**
 * Defines the restful dataland-qa-service API regarding migration from stored datasets to assembled datasets.
 * This is an internal API.
 */
@RequestMapping("/assembled-dataset-migration")
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
interface AssembledDatasetMigrationApi {
    /**
     * A method to force upload a qa report as a stored qa report for testing.
     * This endpoint is dangerous as it is fully unchecked and should only be used for testing purposes.
     */
    @Operation(
        summary = "Triggers a forced upload of a QA Report as a stored QA Report for testing.",
        description = "The data is stored.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Data stored."),
        ],
    )
    @PostMapping("/stored-qa-report/{dataId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun forceUploadStoredQaReport(
        @PathVariable("dataId") dataId: String,
        @RequestBody data: JsonNode,
    ): ResponseEntity<QaReportMetaInformation>
}
