package org.dataland.datalandqaservice.org.dataland.datalandqaservice.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.api.QaReportApi
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReportMetaInformation
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReportWithMetaInformation
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.QaLogMessageBuilder
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.QaReportMetaInformationManager
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils.IdUtils.generateCorrelationId
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import java.util.*

open class QaReportController<QaReportType>(
    var objectMapper: ObjectMapper,
    var qaReportMetaInformationManager: QaReportMetaInformationManager,
    var qaReportManager: QaReportManager,
    private val clazz: Class<QaReportType>,
) : QaReportApi<QaReportType> {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val qaLogMessageBuilder = QaLogMessageBuilder()
    override fun createQaReport(dataId: String, qaReport: QaReportType): ResponseEntity<QaReportMetaInformation> {
        TODO("Not yet implemented")
    }

    override fun getQaReport(qaReportId: String): ResponseEntity<QaReportWithMetaInformation<QaReportType>> {
        // Since we are only starting with Sfdr, we don't need to check for private frameworks (vsme)
        val metaInfoEntity = qaReportMetaInformationManager.getDataMetaInformationByQaReportId(qaReportId)
        val dataId = metaInfoEntity.dataId
        val correlationId = generateCorrelationId(qaReportId, dataId)
        logger.info(qaLogMessageBuilder.getQaReportMessage(qaReportId, dataId))
        val qaReportWithMetaInformation = QaReportWithMetaInformation(
            metaInfo = QaReportMetaInformation(
                dataId = metaInfoEntity.dataId,
                qaReportId = metaInfoEntity.qaReportId,
                reporterUserId = metaInfoEntity.reporterUserId,
                uploadTime = metaInfoEntity.uploadTime,
            ),
            report = objectMapper.readValue(qaReportManager.getQaReport(qaReportId, correlationId).report, clazz),
            // ToDO: implement qaReportManager
        )
        logger.info(
            qaLogMessageBuilder.getQaReportSuccessMessage(qaReportId, dataId, correlationId),
        )
        return ResponseEntity.ok(qaReportWithMetaInformation)
    }

    override fun updateQaReport(dataId: String, qaReportId: String): ResponseEntity<QaReportWithMetaInformation<QaReportType>> {
        TODO("Not yet implemented")
    }

    override fun getAllQaReportsForDataset(
        dataId: String,
        reporterUserId: String?,
    ): ResponseEntity<List<QaReportWithMetaInformation<QaReportType>>> {
        val metaInfoEntities = qaReportMetaInformationManager.searchQaReportMetaInfo(dataId, reporterUserId)
        val listOfQaReportWithMetaInfo = mutableListOf<QaReportWithMetaInformation<QaReportType>>()
        metaInfoEntities.forEach {
            val correlationId = generateCorrelationId(dataId = dataId, qaReportId = null)
            val reportAsString = qaReportManager.getQaReport(
                it.qaReportId,
                correlationId,
            ).report
            listOfQaReportWithMetaInfo.add(
                QaReportWithMetaInformation(
                    metaInfo = QaReportMetaInformation(
                        dataId = it.dataId,
                        qaReportId = it.qaReportId,
                        reporterUserId = it.reporterUserId,
                        uploadTime = it.uploadTime,
                    ),
                    report = objectMapper.readValue(reportAsString, clazz),
                ),
            )
        }
        return ResponseEntity.ok(listOfQaReportWithMetaInfo)
    }
}
