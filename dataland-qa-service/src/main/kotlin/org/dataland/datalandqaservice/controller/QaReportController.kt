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
import java.time.Instant

/**
 * A REST controller for the QA report API.
 */
open class QaReportController<QaReportType>(
    private val objectMapper: ObjectMapper,
    private val qaReportManager: QaReportManager,
    private val clazz: Class<QaReportType>,
    private val dataType: String,
) : QaReportApi<QaReportType> {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val qaLogMessageBuilder = QaLogMessageBuilder()

    override fun postQaReport(dataId: String, qaReport: QaReportType): ResponseEntity<QaReportMetaInformation> {
        val reporterUserId = DatalandAuthentication.fromContext().userId
        logger.info(qaLogMessageBuilder.postQaReportMessage(dataId, reporterUserId))
        val uploadTime = Instant.now().toEpochMilli()
        val reportEntity = qaReportManager.createQaReport(
            report = qaReport,
            dataId = dataId,
            dataType = dataType,
            reporterUserId = reporterUserId,
            uploadTime = uploadTime,
        )
        val apiModel = reportEntity.toApiModel(DatalandAuthentication.fromContextOrNull())
        return ResponseEntity.ok(apiModel)
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

    override fun markQaReportInactive(dataId: String, qaReportId: String) {
        qaReportManager.setQaReportStatus(
            dataId = dataId,
            dataType = dataType,
            qaReportId = qaReportId,
            active = false,
            requestingUser = DatalandAuthentication.fromContext(),
        )
    }

    override fun getAllQaReportsForDataset(
        dataId: String,
        showInactive: Boolean?,
        reporterUserId: String?,
    ): ResponseEntity<List<QaReportWithMetaInformation<QaReportType>>> {
        logger.info(qaLogMessageBuilder.getAllQaReportsForDataIdMessage(dataId, reporterUserId))
        val reportEntities = qaReportManager.searchQaReportMetaInfo(
            dataId = dataId,
            dataType = dataType,
            reporterUserId = reporterUserId,
            showInactive = showInactive ?: false,
        )
        val apiModel = reportEntities.map {
            it.toFullApiModel(objectMapper, clazz, DatalandAuthentication.fromContextOrNull())
        }

        return ResponseEntity.ok(apiModel)
    }
}
