package org.dataland.datalandqaservice.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.BackendOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DataPointQaReviewInformation
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.QaReviewResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import java.util.UUID

/**
 * Defines the restful dataland qa service API
 */
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
interface QaApi {
    /**
     * Gets meta info objects for each dataset.
     */
    @Operation(
        summary = "Get relevant meta info on datasets.",
        description = "Gets a filtered and chronologically ordered list of relevant meta info on datasets.",
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
    @PreAuthorize("hasRole('ROLE_REVIEWER')")
    fun getInfoOnDatasets(
        @RequestParam
        @Parameter(
            name = "dataTypes",
            description = GeneralOpenApiDescriptionsAndExamples.DATA_TYPE_DESCRIPTION,
            required = false,
        )
        dataTypes: Set<DataTypeEnum>?,
        @RequestParam
        @Parameter(
            name = "reportingPeriods",
            description = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_DESCRIPTION,
            example = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE,
            required = false,
        )
        reportingPeriods: Set<String>?,
        @RequestParam
        @Parameter(
            name = "companyName",
            description = GeneralOpenApiDescriptionsAndExamples.COMPANY_NAME_DESCRIPTION,
            example = GeneralOpenApiDescriptionsAndExamples.COMPANY_NAME_EXAMPLE,
            required = false,
        )
        companyName: String?,
        @RequestParam(defaultValue = "Pending")
        @Parameter(
            name = "qaStatus",
            description = GeneralOpenApiDescriptionsAndExamples.QA_STATUS_DESCRIPTION,
            required = false,
        )
        qaStatus: QaStatus,
        @RequestParam(defaultValue = "10")
        @Parameter(
            description = GeneralOpenApiDescriptionsAndExamples.CHUNK_SIZE_DESCRIPTION,
            required = false,
        )
        chunkSize: Int,
        @RequestParam(defaultValue = "0")
        @Parameter(
            description = GeneralOpenApiDescriptionsAndExamples.CHUNK_INDEX_DESCRIPTION,
            required = false,
        )
        chunkIndex: Int,
    ): ResponseEntity<List<QaReviewResponse>>

    /**
     * A method to get the QA review status of an uploaded dataset for a given identifier
     * @param dataId the dataId
     */
    @Operation(
        summary = "Get the QA review information of an uploaded dataset for a given id.",
        description =
            "Get the QA review information of uploaded dataset for a given id." +
                "Users can get the review information of their own datasets." +
                "Admins and reviewer can get the review information for all datasets.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Found QA review information corresponding the id."),
            ApiResponse(responseCode = "404", description = "Found no QA review information corresponding the id."),
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
    fun getQaReviewResponseByDataId(
        @Parameter(
            name = "dataId",
            description = BackendOpenApiDescriptionsAndExamples.DATA_ID_DESCRIPTION,
            example = BackendOpenApiDescriptionsAndExamples.DATA_ID_EXAMPLE,
            required = true,
        )
        @PathVariable("dataId") dataId: UUID,
    ): ResponseEntity<QaReviewResponse>

    /**
     * Changes the QA review status of a dataset
     * @param dataId the ID of the dataset of which to change the QA review status
     * @param qaStatus the QA review status to be assigned to a dataset
     * @param comment (optional) comment to explain the QA review status change
     */
    @Operation(
        summary = "Assign a QA review status to a dataset.",
        description =
            "Assign a QA review status to a dataset.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully assigned QA review status to dataset."),
        ],
    )
    @PostMapping(
        value = ["/datasets/{dataId}"],
    )
    @PreAuthorize("hasRole('ROLE_REVIEWER')")
    fun changeQaStatus(
        @Parameter(
            name = "dataId",
            description = BackendOpenApiDescriptionsAndExamples.DATA_ID_DESCRIPTION,
            example = BackendOpenApiDescriptionsAndExamples.DATA_ID_EXAMPLE,
            required = true,
        )
        @PathVariable("dataId") dataId: String,
        @RequestParam
        @Parameter(
            name = "qaStatus",
            description = GeneralOpenApiDescriptionsAndExamples.QA_STATUS_DESCRIPTION,
            required = true,
        )
        qaStatus: QaStatus,
        @RequestParam
        @Parameter(
            name = "comment",
            description = BackendOpenApiDescriptionsAndExamples.COMMENT_DESCRIPTION,
            required = false,
        )
        comment: String? = null,
        @RequestParam
        @Parameter(
            name = "overwriteDataPointQaStatus",
            description = BackendOpenApiDescriptionsAndExamples.OVERWRITE_DATA_POINT_QA_STATUS_DESCRIPTION,
            required = false,
        )
        overwriteDataPointQaStatus: Boolean = false,
    )

    /** A method to count open reviews based on specific filters.
     * @param dataTypes If set, only the unreviewed datasets with a data type in dataType are counted
     * @param reportingPeriods If set, only the unreviewed datasets with this reportingPeriod are counted
     * @param companyName If set, only the unreviewed datasets for this company are counted
     * @return The number of unreviewed datasets that match the filter
     */
    @Operation(
        summary = "Get the number of unreviewed datasets based on filters.",
        description = "Get the number of unreviewed datasets based on filters.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved the number of unreviewed datasets.",
            ),
        ],
    )
    @GetMapping(
        value = ["/numberOfUnreviewedDatasets"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_REVIEWER')")
    fun getNumberOfPendingDatasets(
        @RequestParam
        @Parameter(
            name = "dataTypes",
            description = GeneralOpenApiDescriptionsAndExamples.DATA_TYPE_DESCRIPTION,
            required = false,
        )
        dataTypes: Set<DataTypeEnum>?,
        @RequestParam
        @Parameter(
            name = "reportingPeriods",
            description = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_DESCRIPTION,
            example = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE,
            required = false,
        )
        reportingPeriods: Set<String>?,
        @RequestParam
        @Parameter(
            name = "companyName",
            description = GeneralOpenApiDescriptionsAndExamples.COMPANY_NAME_DESCRIPTION,
            example = GeneralOpenApiDescriptionsAndExamples.COMPANY_NAME_EXAMPLE,
            required = false,
        )
        companyName: String?,
    ): ResponseEntity<Int>

    /**
     * Get QA review entries for data points.
     */
    @Operation(
        summary = "Get QA review information for data points filtered by various filters.",
        description = "Gets a filtered and chronologically ordered list of relevant QA review information.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved data point QA review information."),
        ],
    )
    @GetMapping(
        value = ["/data-points"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_REVIEWER')")
    fun getDataPointQaReviewInformation(
        @RequestParam
        @Parameter(
            name = "companyName",
            description = GeneralOpenApiDescriptionsAndExamples.COMPANY_NAME_DESCRIPTION,
            example = GeneralOpenApiDescriptionsAndExamples.COMPANY_NAME_EXAMPLE,
            required = false,
        )
        companyId: String?,
        @RequestParam
        @Parameter(
            name = "dataType",
            description = GeneralOpenApiDescriptionsAndExamples.DATA_TYPE_DESCRIPTION,
            required = false,
        )
        dataType: String?,
        @RequestParam
        @Parameter(
            name = "reportingPeriod",
            description = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_DESCRIPTION,
            example = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE,
            required = false,
        )
        reportingPeriod: String?,
        @RequestParam
        @Parameter(
            name = "qaStatus",
            description = GeneralOpenApiDescriptionsAndExamples.QA_STATUS_DESCRIPTION,
            required = false,
        )
        qaStatus: QaStatus?,
        @RequestParam(defaultValue = "true")
        @Parameter(
            name = "showOnlyActive",
            description = BackendOpenApiDescriptionsAndExamples.SHOW_ONLY_ACTIVE_DESCRIPTION,
        )
        showOnlyActive: Boolean,
        @RequestParam(defaultValue = "10")
        @Parameter(
            description = GeneralOpenApiDescriptionsAndExamples.CHUNK_SIZE_DESCRIPTION,
            required = false,
        )
        chunkSize: Int,
        @RequestParam(defaultValue = "0")
        @Parameter(
            description = GeneralOpenApiDescriptionsAndExamples.CHUNK_INDEX_DESCRIPTION,
            required = false,
        )
        chunkIndex: Int,
    ): ResponseEntity<List<DataPointQaReviewInformation>>

    /**
     * A method to get the QA review status of an uploaded data point for a given identifier
     * @param dataPointId the dataId
     */
    @Operation(
        summary = "Get the QA review information of an uploaded data point for a given id.",
        description =
            "Get the QA review information of uploaded data point for a given id." +
                "Users can get the review information of their own data points." +
                "Admins and reviewer can get the review information for all data points.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Found QA review information corresponding the id."),
            ApiResponse(responseCode = "404", description = "Found no QA review information corresponding the id."),
        ],
    )
    @GetMapping(
        value = ["/data-points/{dataPointId}"],
        produces = ["application/json"],
    )
    @PreAuthorize(
        "hasRole('ROLE_REVIEWER') " +
            "or hasRole('ROLE_ADMIN') " +
            "or @SecurityUtilsService.userAskingQaReviewStatusOfOwnDataset(#dataPointId)",
    )
    fun getDataPointQaReviewInformationByDataId(
        @Parameter(
            name = "dataPointId",
            description = BackendOpenApiDescriptionsAndExamples.DATA_POINT_ID_DESCRIPTION,
            example = BackendOpenApiDescriptionsAndExamples.DATA_POINT_ID_EXAMPLE,
            required = true,
        )
        @PathVariable("dataPointId") dataPointId: String,
    ): ResponseEntity<List<DataPointQaReviewInformation>>

    /**
     * Changes the QA review status of a dataset
     * @param dataPointId the ID of the dataset of which to change the QA review status
     * @param qaStatus the QA review status to be assigned to a dataset
     * @param comment (optional) comment to explain the QA review status change
     */
    @Operation(
        summary = "Assign a QA review status to a dataset.",
        description =
            "Assign a QA review status to a dataset.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully assigned QA review status to dataset."),
        ],
    )
    @PostMapping(
        value = ["/data-points/{dataPointId}"],
    )
    @PreAuthorize("hasRole('ROLE_REVIEWER')")
    fun changeDataPointQaStatus(
        @Parameter(
            name = "dataPointId",
            description = BackendOpenApiDescriptionsAndExamples.DATA_POINT_ID_DESCRIPTION,
            example = BackendOpenApiDescriptionsAndExamples.DATA_POINT_ID_EXAMPLE,
            required = true,
        )
        @PathVariable("dataPointId") dataPointId: String,
        @RequestParam
        @Parameter(
            name = "qaStatus",
            description = GeneralOpenApiDescriptionsAndExamples.QA_STATUS_DESCRIPTION,
            required = true,
        )
        qaStatus: QaStatus,
        @RequestParam
        @Parameter(
            name = "comment",
            description = BackendOpenApiDescriptionsAndExamples.COMMENT_DESCRIPTION,
        )
        comment: String? = null,
    )

    /**
     * Get a list of QA review items in reverse chronological order
     */
    @Operation(
        summary = "Get the content of the data point review queue.",
        description = "Retrieves an ordered list of all QA review items currently in status 'Pending' in reverse chronological order.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved queue of QA review items for data points."),
        ],
    )
    @GetMapping(
        value = ["/data-points/queue"],
    )
    @PreAuthorize("hasRole('ROLE_REVIEWER')")
    fun getDataPointReviewQueue(): ResponseEntity<List<DataPointQaReviewInformation>>
}
