package org.dataland.datalandqaservice.org.dataland.datalandqaservice.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.api.QaReportApi
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReportMetaInformation
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReportWithMetaInformation
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.QaReportMetaInformationManager
import org.springframework.http.ResponseEntity
import java.util.*

open class QaReportController<QaReportType>(
    var objectMapper: ObjectMapper,
    var qaReportMetaInformationManager: QaReportMetaInformationManager,
    private val clazz: Class<QaReportType>,
) : QaReportApi<QaReportType> {
    override fun createQaReport(dataId: UUID, qaReport: QaReportType): ResponseEntity<QaReportMetaInformation> {
        TODO("Not yet implemented")
    }

    override fun getQaReport(dataId: UUID, qaReportId: UUID): ResponseEntity<QaReportWithMetaInformation<QaReportType>> {
        //Since we are only starting with Sfdr, we don't need to check for private frameworks (vsme)
        val metaInfoEntity = qaReportMetaInformationManager.getDataMetaInformationByQaReportId(qaReportId)
        val qaReportWithMetaInformation = QaReportWithMetaInformation(
            metaInfo = QaReportMetaInformation(
                dataId = metaInfoEntity.dataId,
                qaReportId = metaInfoEntity.qaReportId,
                reporterUserId = metaInfoEntity.reporterUserId,
                uploadTime = metaInfoEntity.uploadTime,
            ),
            report = objectMapper.readValue()
            //wip
        )
    }

    override fun updateQaReport(dataId: UUID, qaReportId: UUID): ResponseEntity<QaReportWithMetaInformation<QaReportType>> {
        TODO("Not yet implemented")
    }

    override fun getAllQaReportsForDataset(dataId: UUID): ResponseEntity<List<QaReportWithMetaInformation<QaReportType>>> {

    }
}
