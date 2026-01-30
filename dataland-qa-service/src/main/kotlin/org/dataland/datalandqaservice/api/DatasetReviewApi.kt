package org.dataland.datalandqaservice.org.dataland.datalandqaservice.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetReview
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetReviewState
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable

/**
 * Defines the restful dataland dataset review API
 */
interface DatasetReviewApi {
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
        ],
    )
    @PatchMapping(
        value = ["/{datasetReviewId}/reviewer"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun setReviewer(
        @PathVariable datasetReviewId: String,
    ): ResponseEntity<DatasetReview>

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
        ],
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun setState(
        @PathVariable datasetReviewId: String,
        @Parameter(
            name = "datasetReviewState",
            required = true,
        )
        state: DatasetReviewState,
    ): ResponseEntity<DatasetReview>

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
        ],
    )
    @PatchMapping(
        value = ["/{datasetReviewId}/approvedDataPoints"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun acceptOriginalDatapoint(
        @PathVariable datasetReviewId: String,
        @Parameter(
            name = "dataPointId",
            required = true,
        )
        dataPointId: String,
    ): ResponseEntity<DatasetReview>

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
        ],
    )
    @PatchMapping(
        value = ["/{datasetReviewId}/approvedQaReports"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun acceptQaReport(
        @PathVariable datasetReviewId: String,
        @Parameter(
            name = "qaReportId",
            required = true,
        )
        qaReportId: String,
    ): ResponseEntity<DatasetReview>

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
        ],
    )
    @PatchMapping(
        value = ["/{datasetReviewId}/approvedCustomDataPoints"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
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
    ): ResponseEntity<DatasetReview>
}
