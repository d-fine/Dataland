package org.dataland.datalandqaservice.org.dataland.datalandqaservice.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.BackendOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.QaServiceOpenApiDescriptionsAndExamples
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetJudgementResponse
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetJudgementState
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.JudgementDetailsPatch
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * Defines the restful dataland dataset judgement API
 */
@RequestMapping("/dataset-judgements")
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
interface DatasetJudgementApi {
    /**
     * A method to retrieve a dataset judgement object
     * @param datasetJudgementId id of the dataset judgement entity
     */
    @Operation(
        summary = "Get a dataset judgement object by its id.",
        description = "Get a dataset judgement object by its id.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved dataset judgement object."),
            ApiResponse(responseCode = "403", description = "Only admins and judges can access dataset judgement objects."),
        ],
    )
    @GetMapping(
        value = ["/{datasetJudgementId}"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_JUDGE')")
    fun getDatasetJudgement(
        @PathVariable @Parameter(
            description = QaServiceOpenApiDescriptionsAndExamples.DATA_JUDGEMENT_ID_DESCRIPTION,
            example = QaServiceOpenApiDescriptionsAndExamples.DATA_JUDGEMENT_ID_EXAMPLE,
        )
        datasetJudgementId: String,
    ): ResponseEntity<DatasetJudgementResponse>

    /**
     * A method to store a new dataset judgement object
     * @param datasetId the dataset for which the judgement should be created
     */
    @Operation(
        summary = "Upload new dataset judgement object.",
        description = "Upload a new dataset judgement object.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Successfully added dataset judgement object to Dataland."),
            ApiResponse(responseCode = "403", description = "Only admins and judges can access dataset judgement objects."),
            ApiResponse(responseCode = "409", description = "A pending judgement already exists for this dataset."),
        ],
    )
    @PostMapping(
        value = ["/{datasetId}"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_JUDGE')")
    fun postDatasetJudgement(
        @PathVariable @Parameter(
            description = BackendOpenApiDescriptionsAndExamples.DATA_ID_DESCRIPTION,
            example = BackendOpenApiDescriptionsAndExamples.DATA_ID_EXAMPLE,
        )
        datasetId: String,
    ): ResponseEntity<DatasetJudgementResponse>

    /**
     * @param datasetId identifier used to uniquely specify the dataset
     */
    @Operation(
        summary = "Get dataset judgement objects by dataset id.",
        description = "Retrieve all dataset judgement objects associated with the specified dataset id.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved dataset judgement objects."),
            ApiResponse(responseCode = "403", description = "Only admins and judges can access dataset judgement objects."),
        ],
    )
    @GetMapping(
        value = ["/{datasetId}/datasetId"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_JUDGE')")
    fun getDatasetJudgementsByDatasetId(
        @PathVariable @Parameter(
            description = BackendOpenApiDescriptionsAndExamples.DATA_ID_DESCRIPTION,
            example = BackendOpenApiDescriptionsAndExamples.DATA_ID_EXAMPLE,
        )
        datasetId: String,
    ): ResponseEntity<List<DatasetJudgementResponse>>

    /**
     * @param datasetJudgementId identifier used to uniquely specify the data judgement object
     */
    @Operation(
        summary = "Change the Judge of a dataset judgement object.",
        description = "Set yourself as the judge of the dataset judgement object. Other users cannot modify this object.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully set yourself as the judge."),
            ApiResponse(responseCode = "403", description = "Only admins and judges can access dataset judgement objects."),
        ],
    )
    @PatchMapping(
        value = ["/{datasetJudgementId}/judge"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_JUDGE')")
    fun setJudge(
        @PathVariable @Parameter(
            description = QaServiceOpenApiDescriptionsAndExamples.DATA_JUDGEMENT_ID_DESCRIPTION,
            example = QaServiceOpenApiDescriptionsAndExamples.DATA_JUDGEMENT_ID_EXAMPLE,
        )
        datasetJudgementId: String,
    ): ResponseEntity<DatasetJudgementResponse>

    /**
     * @param datasetJudgementId identifier used to uniquely specify the data judgement object
     * @param state DatasetJudgementState to patch to
     */
    @Operation(
        summary = "Change the state of a dataset judgement object.",
        description =
            "Modify state of the dataset judgement object. Approves or rejects associated data set. " +
                "Uploads new datapoints and approves them, rejects unneeded datapoints.",
    )
    @PatchMapping(
        value = ["/{datasetJudgementId}/state"],
        produces = ["application/json"],
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully set state."),
            ApiResponse(
                responseCode = "403",
                description =
                    "Forbidden. You must first assign yourself as the judge " +
                        "for this object via the appropriate PATCH endpoint before editing it.",
            ),
        ],
    )
    @PreAuthorize("@SecurityUtilsService.canUserPatchDatasetJudgement(#datasetJudgementId)")
    fun setJudgementState(
        @PathVariable @Parameter(
            description = QaServiceOpenApiDescriptionsAndExamples.DATA_JUDGEMENT_ID_DESCRIPTION,
            example = QaServiceOpenApiDescriptionsAndExamples.DATA_JUDGEMENT_ID_EXAMPLE,
        )
        datasetJudgementId: String,
        @Valid
        @Parameter(
            description = QaServiceOpenApiDescriptionsAndExamples.DATA_JUDGEMENT_STATE_DESCRIPTION,
        )
        @RequestParam(
            name = "datasetJudgementState",
            required = true,
        )
        state: DatasetJudgementState,
    ): ResponseEntity<DatasetJudgementResponse>

    /**
     * @param datasetJudgementId identifier used to uniquely specify the data judgement object
     * @param dataPointType the type of the data point for which the accepted source should be set
     */
    @Operation(
        summary = "Patch judgement details for a data point",
        description =
            "Updates acceptedSource, companyIdOfAcceptedQaReport and customValue of a data point within a dataset judgement.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully patched judgement details."),
            ApiResponse(
                responseCode = "403",
                description =
                    "Forbidden. You must first assign yourself as the judge " +
                        "for this object via the appropriate PATCH endpoint before editing it.",
            ),

        ],
    )
    @PatchMapping(
        value = ["/{datasetJudgementId}/data-points/{dataPointType}"],
        produces = ["application/json"],
    )
    @PreAuthorize("@SecurityUtilsService.canUserPatchDatasetJudgement(#datasetJudgementId)")
    fun patchJudgementDetails(
        @PathVariable@Parameter(
            description = QaServiceOpenApiDescriptionsAndExamples.DATA_JUDGEMENT_ID_DESCRIPTION,
            example = QaServiceOpenApiDescriptionsAndExamples.DATA_JUDGEMENT_ID_EXAMPLE,
        )
        datasetJudgementId: String,
        @PathVariable@Parameter(
            name = "dataPointType",
            required = true,
            description = BackendOpenApiDescriptionsAndExamples.DATA_POINT_TYPE_DESCRIPTION,
            example = BackendOpenApiDescriptionsAndExamples.DATA_POINT_TYPE_EXAMPLE,
        )
        dataPointType: String,
        @RequestBody
        patch: JudgementDetailsPatch,
    ): ResponseEntity<DatasetJudgementResponse>
}
