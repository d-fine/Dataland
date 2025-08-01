package org.dataland.datalandqaservice.org.dataland.datalandqaservice.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.dataland.datalandbackend.openApiClient.model.QaStatus
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.BackendOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DataAndQaReportMetadata
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.time.LocalDate
import java.util.UUID

/**
 * Defines the restful dataland QAReports Metadata API
 */
@Suppress("kotlin:S6517")
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
interface QaReportsMetadataApi {
    /**
     * A method to retrieve metadata of QA reports based on filters.
     * @param uploaderUserIds If set, only metadata of reports that pertain to datasets
     *  from these uploaders will be returned.
     *  in uploaderUserIds.
     * @param showOnlyActive If true or empty, only metadata of reports are returned that are active.
     *  If false, reports will be returned regardless of their active status.
     * @param qaStatus If set, only metadata of reports are returned that relate to a dataset with this qaStatus
     * @param minUploadDate If set, only metadata of reports are returned that were uploaded after the minUploadDate.
     * @param maxUploadDate If set, only metadata of reports are returned that were uploaded before the maxUploadDate.
     */
    @Operation(
        summary = "Get metadata of QA reports based on filters.",
        description =
            "Get metadata of QA reports based on filters. " +
                "Users can search for QA reports posted within a specific time frame, posted by specific uploaders, " +
                "for QA reports with a specific QA status, or for active QA reports only. The filters can be combined.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Found QA reports for applied filters."),
        ],
    )
    @GetMapping(
        value = ["/data/reports/metadata"],
        produces = ["application/json"],
    )
    fun getQaReportsMetadata(
        @RequestParam
        @Parameter(
            description = BackendOpenApiDescriptionsAndExamples.REPORTER_USER_ID_DESCRIPTION,
            example = BackendOpenApiDescriptionsAndExamples.REPORTER_USER_ID_EXAMPLE,
            required = false,
        )
        uploaderUserIds: Set<UUID>? = null,
        @RequestParam(defaultValue = "true")
        @Parameter(
            description = BackendOpenApiDescriptionsAndExamples.QA_REPORT_SHOW_ONLY_ACTIVE_DESCRIPTION,
            required = false,
        )
        showOnlyActive: Boolean,
        @RequestParam
        @Parameter(
            description = GeneralOpenApiDescriptionsAndExamples.QA_STATUS_DESCRIPTION,
            required = false,
        )
        qaStatus: QaStatus? = null,
        @RequestParam
        @Parameter(
            description = BackendOpenApiDescriptionsAndExamples.QA_REPORT_MIN_UPLOAD_DATE_DESCRIPTION,
            example = BackendOpenApiDescriptionsAndExamples.QA_REPORT_MIN_UPLOAD_DATE_EXAMPLE,
            required = false,
        )
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        minUploadDate: LocalDate? = null,
        @RequestParam
        @Parameter(
            description = BackendOpenApiDescriptionsAndExamples.QA_REPORT_MAX_UPLOAD_DATE_DESCRIPTION,
            example = BackendOpenApiDescriptionsAndExamples.QA_REPORT_MAX_UPLOAD_DATE_EXAMPLE,
            required = false,
        )
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        maxUploadDate: LocalDate? = null,
        @RequestParam
        @Parameter(
            description = GeneralOpenApiDescriptionsAndExamples.COMPANY_SINGLE_IDENTIFIER_DESCRIPTION,
            example = GeneralOpenApiDescriptionsAndExamples.COMPANY_SINGLE_IDENTIFIER_EXAMPLE,
            required = false,
        )
        companyIdentifier: String?,
    ): ResponseEntity<List<DataAndQaReportMetadata>>
}
