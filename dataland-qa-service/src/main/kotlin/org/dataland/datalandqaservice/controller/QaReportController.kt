package org.dataland.datalandqaservice.org.dataland.datalandqaservice.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.api.QaReportApi
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReportMetaInformation
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReportWithMetaInformation
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.QaLogMessageBuilder
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.QaReportManager
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import java.util.*

open class QaReportController<QaReportType>(
    private val objectMapper: ObjectMapper,
    private val qaReportManager: QaReportManager,
    private val clazz: Class<QaReportType>,
    private val dataType: String,
) : QaReportApi<QaReportType> {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val qaLogMessageBuilder = QaLogMessageBuilder()

    override fun createQaReport(dataId: String, qaReport: QaReportType): ResponseEntity<QaReportMetaInformation> {
        TODO("Not yet implemented")
    }

    override fun getQaReport(
        dataId: String,
        qaReportId: String,
    ): ResponseEntity<QaReportWithMetaInformation<QaReportType>> {
        logger.info(qaLogMessageBuilder.getQaReportMessage(qaReportId, dataId))
        val reportEntity = qaReportManager.getQaReportById(dataId, dataType, qaReportId)
        val apiModel = reportEntity.toFullApiModel(objectMapper, clazz, DatalandAuthentication.fromContextOrNull())
        return ResponseEntity.ok(apiModel)
    }

    override fun updateQaReport(dataId: String, qaReportId: String): ResponseEntity<QaReportWithMetaInformation<QaReportType>> {
        TODO("Not yet implemented")
    }

    override fun getAllQaReportsForDataset(
        dataId: String,
        reporterUserId: String?,
    ): ResponseEntity<List<QaReportWithMetaInformation<QaReportType>>> {
        logger.info(qaLogMessageBuilder.getAllQaReportsForDataIdMessage(dataId, reporterUserId))
        val reportEntities = qaReportManager.searchQaReportMetaInfo(dataId, dataType, reporterUserId)
        val apiModel = reportEntities.map {
            it.toFullApiModel(objectMapper, clazz, DatalandAuthentication.fromContextOrNull())
        }

        return ResponseEntity.ok(apiModel)
    }
}
