package org.dataland.datalandqaservice.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.ReviewInformationResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.util.*

/**
 * Defines the restful dataland qa service API
 */
@RequestMapping("/qa")
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
interface QaApi {
    /**
     * Get an ordered list of datasets to be QAed
     */
    @Operation(
        summary = "Get unreviewed metadata sets of qa reports.",
        description = "Gets a ordered list of metadata sets which need to be reviewed",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved metadata sets."),
        ],
    )
    @GetMapping(
        value = ["/datasets"],
        produces = ["application/json"],
    )
    // TODO We should discuss the naming of the endpoint, its not fully metadatasets that are being reviewed
    @PreAuthorize("hasRole('ROLE_REVIEWER')")
    fun getUnreviewedMetadataSets(
        @RequestParam dataType: Set<DataTypeEnum>?,
        @RequestParam reportingPeriod: Set<String>?,
        @RequestParam companyName: String?,
        @RequestParam(defaultValue = "100") chunkSize: Int,
        @RequestParam(defaultValue = "0") chunkIndex: Int,
    ): ResponseEntity<List<ReviewInformationResponse>>

    /**
     * A method to get the QA review status of an uploaded dataset for a given identifier
     * @param dataId the dataId
     */
    @Operation(
        summary = "Gets the QA review status of an uploaded dataset for a given id.",
        description = "Get the QA review status of uploaded dataset for a given id." +
            "Users can get the review status of their own datasets." +
            "Admins and reviewer can get the review status for all datasets.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Found a QA review status corresponding the id."),
            ApiResponse(responseCode = "404", description = "Found no QA review status corresponding the id."),
        ],
    )
    @GetMapping(
        value = ["/datasets/{dataId}"],
        produces = ["application/json"],
    )
    @PreAuthorize(
        "hasRole('ROLE_REVIEWER') " +
            "or hasRole('ROLE_ADMIN') " +
            "or @SecurityUtilsService.userAskingQaReviewStatusOfOwnDataset(#dataId)",
    )
    fun getDatasetById(
        @PathVariable("dataId") dataId: UUID,
    ): ResponseEntity<ReviewInformationResponse>

    /**
     * Assigns a quality status to a unreviewed dataset
     * @param dataId the ID of the dataset of which to change the quality status
     * @param qaStatus the quality status to be assigned to a dataset
     * @param message (optional) message to be assigned to a dataset
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
    fun assignQaStatus(
        @PathVariable("dataId") dataId: String,
        @RequestParam qaStatus: QaStatus,
        @RequestParam message: String? = null,
    )
}
