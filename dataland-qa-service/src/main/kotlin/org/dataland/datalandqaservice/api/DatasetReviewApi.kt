package org.dataland.datalandqaservice.org.dataland.datalandqaservice.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.BackendOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.QaServiceOpenApiDescriptionsAndExamples
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetReviewResponse
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetReviewState
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * Defines the restful dataland dataset review API
 */
@RequestMapping("/dataset-reviews")
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
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
        @PathVariable @Parameter(
            description = QaServiceOpenApiDescriptionsAndExamples.DATA_REVIEW_ID_DESCRIPTION,
            example = QaServiceOpenApiDescriptionsAndExamples.DATA_REVIEW_ID_EXAMPLE,
        )
        datasetReviewId: String,
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
        @PathVariable @Parameter(
            description = BackendOpenApiDescriptionsAndExamples.DATA_ID_DESCRIPTION,
            example = BackendOpenApiDescriptionsAndExamples.DATA_ID_EXAMPLE,
        )
        datasetId: String,
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
        @PathVariable @Parameter(
            description = BackendOpenApiDescriptionsAndExamples.DATA_ID_DESCRIPTION,
            example = BackendOpenApiDescriptionsAndExamples.DATA_ID_EXAMPLE,
        )
        datasetId: String,
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
        @PathVariable @Parameter(
            description = QaServiceOpenApiDescriptionsAndExamples.DATA_REVIEW_ID_DESCRIPTION,
            example = QaServiceOpenApiDescriptionsAndExamples.DATA_REVIEW_ID_EXAMPLE,
        )
        datasetReviewId: String,
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
        @PathVariable @Parameter(
            description = QaServiceOpenApiDescriptionsAndExamples.DATA_REVIEW_ID_DESCRIPTION,
            example = QaServiceOpenApiDescriptionsAndExamples.DATA_REVIEW_ID_EXAMPLE,
        )
        datasetReviewId: String,
        @Valid
        @Parameter(
            description = QaServiceOpenApiDescriptionsAndExamples.DATA_REVIEW_STATE_DESCRIPTION,
        )
        @RequestParam(
            name = "datasetReviewState",
            required = true,
        )
        state: DatasetReviewState,
    ): ResponseEntity<DatasetReviewResponse>

    /**
     * @param datasetReviewId identifier used to uniquely specify the data review object
     * @param dataPointType the type of the data point for which the accepted source should be set
     * @param acceptedSource the accepted source to set for the data point
     * @param companyIdOfAcceptedQaReport if the accepted source is a QA report datapoint, the company id of the accepted QA report
     * @param customValue if the accepted source is custom, a custom value needs to be provided
     */
    @Operation(
        summary = "Patch accepted data point source and/or custom value.",
        description =
            "Change the accepted source of a datapoint. " +
                "In case a custom or qa report data point is accepted, provide additional information.",
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
        value = ["/{datasetReviewId}/setApprovedDataPointSource"],
        produces = ["application/json"],
    )
    @PreAuthorize("@SecurityUtilsService.canUserPatchDatasetReview(#datasetReviewId)")
    fun setAcceptedSource(
        @PathVariable@Parameter(
            description = QaServiceOpenApiDescriptionsAndExamples.DATA_REVIEW_ID_DESCRIPTION,
            example = QaServiceOpenApiDescriptionsAndExamples.DATA_REVIEW_ID_EXAMPLE,
        )
        datasetReviewId: String,
        @Parameter(
            name = "dataPointType",
            required = true,
            description = BackendOpenApiDescriptionsAndExamples.DATA_POINT_TYPE_DESCRIPTION,
            example = BackendOpenApiDescriptionsAndExamples.DATA_POINT_TYPE_EXAMPLE,
        )
        dataPointType: String,
        @Parameter(
            name = "acceptedSource",
            required = true,
            description = BackendOpenApiDescriptionsAndExamples.ACCEPTED_SOURCE_DESCRIPTION,
            example = BackendOpenApiDescriptionsAndExamples.ACCEPTED_SOURCE_EXAMPLE,
        )
        acceptedSource: AcceptedDataPointSource,
        @Parameter(
            name = "companyIdOfAcceptedQaReport",
            description = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_DESCRIPTION,
            example = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
        )
        companyIdOfAcceptedQaReport: String?,
        @Valid@Parameter(
            name = "customValue",
            description = BackendOpenApiDescriptionsAndExamples.CUSTOM_VALUE_DESCRIPTION,
            example = BackendOpenApiDescriptionsAndExamples.CUSTOM_VALUE_EXAMPLE,
        )
        customValue: String?,
    ): ResponseEntity<DatasetReviewResponse>
}
