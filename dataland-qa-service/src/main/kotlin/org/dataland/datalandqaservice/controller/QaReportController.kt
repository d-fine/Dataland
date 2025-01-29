package org.dataland.datalandqaservice.org.dataland.datalandqaservice.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.api.QaReportApi
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReportMetaInformation
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReportStatusPatch
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReportWithMetaInformation
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DatasetQaReportService
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.QaLogMessageBuilder
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
    private val qaReportManager: DatasetQaReportService,
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
        val reportApiModel =
            qaReportManager.createQaReport(
                report = qaReport,
                dataId = dataId,
                dataType = dataType,
                reporterUserId = reportingUser.userId,
                uploadTime = uploadTime,
            )
        return ResponseEntity.ok(reportApiModel)
    }

    override fun getQaReport(
        dataId: String,
        qaReportId: String,
    ): ResponseEntity<QaReportWithMetaInformation<QaReportType>> {
        val user = DatalandAuthentication.fromContext()
        logger.info(qaLogMessageBuilder.getQaReportMessage(qaReportId, dataId))
        qaReportSecurityPolicy.ensureUserCanViewQaReportForDataId(dataId, user)
        val reportApiModel = qaReportManager.getQaReportById(dataId, dataType, qaReportId, objectMapper, clazz)
        return ResponseEntity.ok(reportApiModel)
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
        val reportApiModels =
            qaReportManager.searchQaReportMetaInfo(
                dataId = dataId,
                dataType = dataType,
                reporterUserId = reporterUserId,
                showInactive = showInactive ?: false,
                objectMapper = objectMapper,
                clazz = clazz,
            )

        return ResponseEntity.ok(reportApiModels)
    }
}
