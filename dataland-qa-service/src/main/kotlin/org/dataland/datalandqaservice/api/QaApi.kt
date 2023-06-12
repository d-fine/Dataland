package org.dataland.datalandqaservice.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.dataland.datalandbackendutils.model.QaStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * Defines the restful dataland qa service API
 */
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
interface QaApi {
    /**
     * Get an ordered list of datasets to be QAed
     */
    @Operation(
        summary = "Get unreviewed datasets IDs.",
        description = "Gets a ordered list of dataset IDs which need to be reviewed",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved dataset IDs."),
        ],
    )
    @GetMapping(
        value = ["/datasets"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_REVIEWER')")
    fun getUnreviewedDatasetsIds(): ResponseEntity<List<String>>

    /**
     * Assigns a quality status to a unreviewed dataset
     * @param dataId the ID of the dataset of which to change the quality status
     * @param qualityStatus the quality status to be assigned to a dataset
     */
    @Operation(
        summary = "Assign a quality status to a unreviewed dataset",
        description = "Set the quality status after a dataset has been reviewed",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully assigned quality status to dataset."),
        ],
    )
    @PostMapping(
        value = ["/datasets/{dataId}"],
    )
    @PreAuthorize("hasRole('ROLE_REVIEWER')")
    fun assignQualityStatus(
        @PathVariable("dataId") dataId: String,
        @RequestParam qualityStatus: QaStatus,
    ): ResponseEntity<Unit>
}
