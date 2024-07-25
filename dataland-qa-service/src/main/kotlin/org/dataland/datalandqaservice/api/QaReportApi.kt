package org.dataland.datalandqaservice.org.dataland.datalandqaservice.api

import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReportMetaInformation
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReportWithMetaInformation
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import java.util.UUID

/**
 * Defines the restful dataland Qa Report API
 */
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
interface QaReportApi<QaReportType> {

    @PostMapping(
        value = ["/{dataId}/reports"],
        produces = ["application/json"],
    )
    fun createQaReport(
        @PathVariable("dataId") dataId: UUID,
        @RequestBody qaReport: QaReportType,
    ): QaReportMetaInformation

    @PutMapping(
        value = ["/{dataId}/reports/{qaReportId}"],
        produces = ["application/json"],
    )
    fun updateQaReport(@PathVariable("dataId") dataId: UUID, @PathVariable("qaReportId") qaReportId: UUID)
        : QaReportWithMetaInformation<QaReportType>

    @GetMapping(
        value = ["/{dataId}/reports/{qaReportId}"],
        produces = ["application/json"],
    )
   fun getQaReport(@PathVariable("dataId") dataId: UUID, @PathVariable("qaReportId") qaReportId: UUID)
        : QaReportWithMetaInformation<QaReportType>

    @GetMapping(
        value = ["/{dataId}/reports"],
        produces = ["application/json"],
    )
   fun getQaReports(@PathVariable("dataId") dataId: UUID): List<QaReportWithMetaInformation<QaReportType>>
}