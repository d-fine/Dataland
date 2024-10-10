package org.dataland.datalandqaservice.org.dataland.datalandqaservice.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.api.QaReportApi
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReportMetaInformation
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReportStatusPatch
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReportWithMetaInformation
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.QaLogMessageBuilder
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.QaReportManager
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.QaReportSecurityPolicy
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
    private val qaReportSecurityPolicy: QaReportSecurityPolicy,
    private val clazz: Class<QaReportType>,
    private val dataType: String,
) : QaReportApi<QaReportType> {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val qaLogMessageBuilder = QaLogMessageBuilder()

    override fun postQaReport(
        dataId: String,
        qaReport: QaReportType,
    ): ResponseEntity<QaReportMetaInformation> {
        val reportingUser = DatalandAuthentication.fromContext()
        logger.info(qaLogMessageBuilder.postQaReportMessage(dataId, reportingUser.userId))
        qaReportSecurityPolicy.ensureUserCanViewQaReportForDataId(dataId, reportingUser)
        val uploadTime = Instant.now().toEpochMilli()
        val reportEntity =
            qaReportManager.createQaReport(
                report = qaReport,
                dataId = dataId,
                dataType = dataType,
                reporterUserId = reportingUser.userId,
                uploadTime = uploadTime,
            )
        val apiModel = reportEntity.toMetaInformationApiModel()
        return ResponseEntity.ok(apiModel)
    }

    override fun getQaReport(
        dataId: String,
        qaReportId: String,
    ): ResponseEntity<QaReportWithMetaInformation<QaReportType>> {
        val user = DatalandAuthentication.fromContext()
        logger.info(qaLogMessageBuilder.getQaReportMessage(qaReportId, dataId))
        qaReportSecurityPolicy.ensureUserCanViewQaReportForDataId(dataId, user)
        val reportEntity = qaReportManager.getQaReportById(dataId, dataType, qaReportId)
        val apiModel = reportEntity.toFullApiModel(objectMapper, clazz)
        return ResponseEntity.ok(apiModel)
    }

    override fun setQaReportStatus(
        dataId: String,
        qaReportId: String,
        statusPatch: QaReportStatusPatch,
    ) {
        val user = DatalandAuthentication.fromContext()
        logger.info(qaLogMessageBuilder.requestChangeQaReportStatus(qaReportId, dataId, statusPatch.active))
        qaReportManager.setQaReportStatus(
            dataId = dataId,
            dataType = dataType,
            qaReportId = qaReportId,
            statusToSet = statusPatch.active,
            requestingUser = user,
        )
    }

    override fun getAllQaReportsForDataset(
        dataId: String,
        showInactive: Boolean?,
        reporterUserId: String?,
    ): ResponseEntity<List<QaReportWithMetaInformation<QaReportType>>> {
        val user = DatalandAuthentication.fromContext()
        qaReportSecurityPolicy.ensureUserCanViewQaReportForDataId(dataId, user)
        logger.info(qaLogMessageBuilder.getAllQaReportsForDataIdMessage(dataId, reporterUserId))
        val reportEntities =
            qaReportManager.searchQaReportMetaInfo(
                dataId = dataId,
                dataType = dataType,
                reporterUserId = reporterUserId,
                showInactive = showInactive ?: false,
            )
        val apiModel =
            reportEntities.map {
                it.toFullApiModel(objectMapper, clazz)
            }

        return ResponseEntity.ok(apiModel)
    }
}
