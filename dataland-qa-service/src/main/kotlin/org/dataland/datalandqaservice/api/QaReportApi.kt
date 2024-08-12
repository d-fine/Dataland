package org.dataland.datalandqaservice.org.dataland.datalandqaservice.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReportMetaInformation
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReportStatusPatch
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReportWithMetaInformation
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

/**
 * Defines the restful dataland Qa Report API
 */
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
interface QaReportApi<QaReportType> {
    /**
     * A method to store QA reports for a specific dataset on Dataland.
     * The new uploaded QA report is marked as active. All other QA reports for the same dataset are marked as inactive.
     * @param dataId identifier used to uniquely specify data in the data store
     * @param qaReport the QA report to be stored
     * @return meta info about the stored QA report including the ID of the created entry
     */
    @Operation(
        summary = "Upload new QA report.",
        description = "The uploaded QA report is added to the database, the generated report id is returned.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully added QA report to dataland."),
        ],
    )
    @PostMapping(
        value = ["/{dataId}/reports"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_REVIEWER')")
    fun postQaReport(
        @PathVariable("dataId") dataId: String,
        @RequestBody qaReport: QaReportType,
    ): ResponseEntity<QaReportMetaInformation>

    /**
     * A method to change the status of a QA report. Inactive QA reports are still shown in the QA report list.
     * @param dataId identifier used to uniquely specify data in the data store
     * @param qaReportId identifier used to identify the QA report to be retrieved
     */
    @Operation(
        summary = "Change the status of a QA report.",
        description = "The specified QA report is marked as active or inactive.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully marked QA report as active or inactive."),
        ],
    )
    @PatchMapping(
        value = ["/{dataId}/reports/{qaReportId}/status"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_REVIEWER')")
    fun setQaReportStatus(
        @PathVariable("dataId") dataId: String,
        @PathVariable("qaReportId") qaReportId: String,
        @RequestBody statusPatch: QaReportStatusPatch,
    )

    /**
     * A method to retrieve a QA report by the data Id and report Id
     * @param dataId identifier used to uniquely specify data in the data store
     * @param qaReportId identifier used to identify the QA report to be retrieved
     * @return information about the QA report
     */
    @Operation(
        summary = "Retrieve a QA report.",
        description = "Retrieve the information about a QA report.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved QA report."),
        ],
    )
    @GetMapping(
        value = ["/{dataId}/reports/{qaReportId}"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    fun getQaReport(
        @PathVariable("dataId") dataId: String,
        @PathVariable("qaReportId") qaReportId: String,
    ): ResponseEntity<QaReportWithMetaInformation<QaReportType>>

    /**
     * A method to retrieve all QA reports associated with a data ID or reporter user ID
     * @param dataId identifier used to uniquely specify data in the data store
     * @param showInactive flag to indicate if inactive QA reports should be included in the response
     * @param reporterUserId show only QA reports uploaded by the given user
     * @return information about all QA reports associated with the data id
     */
    @Operation(
        summary = "Retrieve all QA reports.",
        description = "Retrieve all QA reports associated to given dataset.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved all QA reports."),
        ],
    )
    @GetMapping(
        value = ["/{dataId}/reports"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    fun getAllQaReportsForDataset(
        @PathVariable("dataId") dataId: String,
        @RequestParam(required = false) showInactive: Boolean?,
        @RequestParam(required = false) reporterUserId: String?,
    ): ResponseEntity<List<QaReportWithMetaInformation<QaReportType>>>
}
