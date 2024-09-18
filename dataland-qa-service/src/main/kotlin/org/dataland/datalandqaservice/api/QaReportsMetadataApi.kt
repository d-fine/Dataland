package org.dataland.datalandqaservice.org.dataland.datalandqaservice.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.dataland.datalandbackend.openApiClient.model.QaStatus
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DataAndQaReportMetadata
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.util.*

/**
 * Defines the restful dataland QAReports Metadata API
 */
@Suppress("kotlin:S6517")
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
interface QaReportsMetadataApi {
    /**
     * A method to retrieve metadata of QA reports based on filters.
     * @param uploaderUserIds If set, only metadata of reports are returned that relate to a dataset of an uploader
     *  in uploaderUserIds.
     * @param showOnlyActive If true, only metadata of reports are returned that are active
     * @param qaStatus If set, only metadata of reports are returned that relate to a dataset with this qaStatus
     * @param startDate If set, only metadata of reports are returned that are past the startDate
     * @param endDate If set, only metadata of reports are returned that are before the endDate
     */
    @Operation(
        summary = "Get metadata of QA reports based on filters.",
        description = "Get metadata of QA reports based on filters." +
            "Users can search for QA reports posted within a specific time frame, posted by specific uploaders," +
            "for QA reports with a specific QA status, or for active QA reports only. The filters can be combined.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Found QA reports for applied filters."),
            ApiResponse(responseCode = "404", description = "Found no QA reports for applied filters."),
        ],
    )
    @GetMapping(
        value = ["/data/reports/metadata"],
        produces = ["application/json"],
    )
    fun getQaReportsMetadata(
        @RequestParam uploaderUserIds: Set<UUID>? = null,
        @RequestParam(defaultValue = "true") showOnlyActive: Boolean,
        @RequestParam qaStatus: QaStatus? = null,
        @RequestParam
        @DateTimeFormat(pattern = "YYYYMMDD")
        startDate: String? = null,
        @RequestParam
        @DateTimeFormat(pattern = "YYYYMMDD")
        endDate: String? = null,
        @RequestParam companyIdentifier: String?,
    ): ResponseEntity<List<DataAndQaReportMetadata>>
}
