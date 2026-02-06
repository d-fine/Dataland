package org.dataland.datalandqaservice.org.dataland.datalandqaservice.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetReviewResponse
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetReviewState
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping

/**
 * Defines the restful dataland dataset review API
 */
interface DatasetReviewApi {
    /**
     * A method to retrieve a dataset review object
     * @param datasetReviewId id of the dataset review entity
     */
    @Operation(
        summary = "Get a dataset review object by its id.",
        description = "Get a dataset review object by its id.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved dataset review object."),
            ApiResponse(responseCode = "403", description = "Only Dataland admins can access dataset review objects."),
        ],
    )
    @GetMapping(
        value = ["/{datasetReviewId}"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun getDatasetReview(
        @PathVariable datasetReviewId: String,
    ): ResponseEntity<DatasetReviewResponse>

    /**
     * A method to store a new dataset review object
     * @param datasetId the dataset for which the review should be created
     */
    @Operation(
        summary = "Upload new dataset review object.",
        description = "Upload a new dataset review object.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Successfully added dataset review object to dataland."),
            ApiResponse(responseCode = "403", description = "Only Dataland admins can access dataset review objects."),
        ],
    )
    @PostMapping(
        value = ["/{datasetId}"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun postDatasetReview(
        @PathVariable datasetId: String,
    ): ResponseEntity<DatasetReviewResponse>

    /**
     * @param datasetId identifier used to uniquely specify the dataset
     */
    @Operation(
        summary = "Get dataset review objects by dataset id.",
        description = "Retrieve all dataset review objects associated with the specified dataset id.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved dataset review objects."),
            ApiResponse(responseCode = "403", description = "Only Dataland admins can access dataset review objects."),
        ],
    )
    @GetMapping(
        value = ["/{datasetId}/datasetId"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun getDatasetReviewsByDatasetId(
        @PathVariable datasetId: String,
    ): ResponseEntity<List<DatasetReviewResponse>>

    /**
     * @param datasetReviewId identifier used to uniquely specify the data review object
     */
    @Operation(
        summary = "Change the reviewer of a dataset review object.",
        description = "Set yourself as the reviewer of the dataset review object. Other users cannot modify this object.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully set yourself as the reviewer."),
            ApiResponse(responseCode = "403", description = "Only Dataland admins can access dataset review objects."),
        ],
    )
    @PatchMapping(
        value = ["/{datasetReviewId}/reviewer"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun setReviewer(
        @PathVariable datasetReviewId: String,
    ): ResponseEntity<DatasetReviewResponse>

    /**
     * @param datasetReviewId identifier used to uniquely specify the data review object
     * @param state DatasetReviewState to patch to
     */
    @Operation(
        summary = "Change the state of a dataset review object.",
        description =
            "Modify state of the dataset review object. Approves or rejects associated data set. " +
                "Uploads new datapoints and approves them, rejects unneeded datapoints.",
    )
    @PatchMapping(
        value = ["/{datasetReviewId}/state"],
        produces = ["application/json"],
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully set state."),
            ApiResponse(
                responseCode = "403",
                description =
                    "Forbidden. You must first assign yourself as the reviewer " +
                        "for this object via the appropriate PATCH endpoint before editing it.",
            ),
        ],
    )
    @PreAuthorize("@SecurityUtilsService.canUserPatchDatasetReview(#datasetReviewId)")
    fun setReviewState(
        @PathVariable datasetReviewId: String,
        @Parameter(
            name = "datasetReviewState",
            required = true,
        )
        state: DatasetReviewState,
    ): ResponseEntity<DatasetReviewResponse>

    /**
     * @param datasetReviewId identifier used to uniquely specify the data review object
     * @param dataPointId identifier used to uniquely specify the datapoint
     */
    @Operation(
        summary = "Put a datapoint to ApprovedDataPoints map.",
        description =
            "Adds a datapoint to the ApprovedDataPoints map. " +
                "Removes the qa report and custom datapoint from approvedQaReports and approvedCustomDataPoints with same data point type.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully approved datapoint."),
            ApiResponse(
                responseCode = "403",
                description =
                    "Forbidden. You must first assign yourself as the reviewer " +
                        "for this object via the appropriate PATCH endpoint before editing it.",
            ),

        ],
    )
    @PatchMapping(
        value = ["/{datasetReviewId}/approvedDataPoints"],
        produces = ["application/json"],
    )
    @PreAuthorize("@SecurityUtilsService.canUserPatchDatasetReview(#datasetReviewId)")
    fun acceptOriginalDatapoint(
        @PathVariable datasetReviewId: String,
        @Parameter(
            name = "dataPointId",
            required = true,
        )
        dataPointId: String,
    ): ResponseEntity<DatasetReviewResponse>

    /**
     * @param datasetReviewId identifier used to uniquely specify the data review object
     * @param qaReportId identifier used to uniquely specify the qa report
     */
    @Operation(
        summary = "Put a qa report to ApprovedQaReports map.",
        description =
            "Adds a datapoint to the ApprovedQaReports map. " +
                "Removes the data point id and custom data point from " +
                "approvedDataPoints and approvedCustomDataPoints with same data point type.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully approved qa report."),
            ApiResponse(
                responseCode = "403",
                description =
                    "Forbidden. You must first assign yourself as the reviewer " +
                        "for this object via the appropriate PATCH endpoint before editing it.",
            ),
        ],
    )
    @PatchMapping(
        value = ["/{datasetReviewId}/approvedQaReports"],
        produces = ["application/json"],
    )
    @PreAuthorize("@SecurityUtilsService.canUserPatchDatasetReview(#datasetReviewId)")
    fun acceptQaReport(
        @PathVariable datasetReviewId: String,
        @Parameter(
            name = "qaReportId",
            required = true,
        )
        qaReportId: String,
    ): ResponseEntity<DatasetReviewResponse>

    /**
     * @param datasetReviewId identifier used to uniquely specify the data review object
     * @param dataPoint datapoint as string in JSON format
     * @param dataPointType type of datapoint according to specifications
     */
    @Operation(
        summary = "Put a custom data point to ApprovedCustomDataPoints map.",
        description =
            "Adds a custom datapoint to the ApprovedCustomDataPoints map. " +
                "Removes the data point id and qa report from approvedDataPoints and approvedQaReports with same data point type.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully approved and saved custom data point."),
            ApiResponse(
                responseCode = "403",
                description =
                    "Forbidden. You must first assign yourself as the reviewer " +
                        "for this object via the appropriate PATCH endpoint before editing it.",
            ),
        ],
    )
    @PatchMapping(
        value = ["/{datasetReviewId}/approvedCustomDataPoints"],
        produces = ["application/json"],
    )
    @PreAuthorize("@SecurityUtilsService.canUserPatchDatasetReview(#datasetReviewId)")
    fun acceptCustomDataPoint(
        @PathVariable datasetReviewId: String,
        @Parameter(
            name = "dataPoint",
            required = true,
        )
        dataPoint: String,
        @Parameter(
            name = "dataPointType",
            required = true,
        )
        dataPointType: String,
    ): ResponseEntity<DatasetReviewResponse>
}
