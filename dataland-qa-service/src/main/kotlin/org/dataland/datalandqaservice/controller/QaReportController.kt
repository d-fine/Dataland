package org.dataland.datalandqaservice.org.dataland.datalandqaservice.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.api.QaReportApi
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReportMetaInformation
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReportWithMetaInformation
import java.util.*


open class QaReportController<QaReportType>(
    var objectMapper: ObjectMapper,
    private val clazz: Class<QaReportType>,
) : QaReportApi<QaReportType> {
    override fun createQaReport(dataId: UUID, qaReport: QaReportType): QaReportMetaInformation {
        TODO("Not yet implemented")
    }

    override fun getQaReport(dataId: UUID, qaReportId: UUID): QaReportWithMetaInformation<QaReportType> {
        TODO("Not yet implemented")
    }

    override fun updateQaReport(dataId: UUID, qaReportId: UUID): QaReportWithMetaInformation<QaReportType> {
        TODO("Not yet implemented")
    }

    override fun getQaReports(dataId: UUID): List<QaReportWithMetaInformation<QaReportType>> {
        TODO("Not yet implemented")
    }
}