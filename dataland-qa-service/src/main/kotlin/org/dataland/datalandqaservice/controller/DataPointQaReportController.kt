package org.dataland.datalandqaservice.org.dataland.datalandqaservice.controller

import org.dataland.datalandqaservice.model.reports.QaReportDataPoint
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.api.DataPointQaReportApi
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.DataPointQaReport
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReportStatusPatch
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DataPointQaReportManager
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.QaLogMessageBuilder
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.QaReportSecurityPolicy
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils.IdUtils
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

/**
 * Controller for the data point QA report.
 */
@RestController
class DataPointQaReportController(
    @Autowired private val qaReportSecurityPolicy: QaReportSecurityPolicy,
    @Autowired private val dataPointQaReportManager: DataPointQaReportManager,
) : DataPointQaReportApi {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val qaLogMessageBuilder = QaLogMessageBuilder()

    override fun postQaReport(
        dataPointId: String,
        qaReport: QaReportDataPoint<String?>,
    ): ResponseEntity<DataPointQaReport> {
        val correlationId = IdUtils.generateUUID()
        val reportingUser = DatalandAuthentication.fromContext()
        logger.info(qaLogMessageBuilder.postQaReportMessage(dataPointId, reportingUser.userId, correlationId))
        qaReportSecurityPolicy.ensureUserCanViewDataPoint(dataPointId, reportingUser)
        val uploadTime = Instant.now().toEpochMilli()
        val report =
            dataPointQaReportManager.createQaReport(
                report = qaReport,
                dataPointId = dataPointId,
                reporterUserId = reportingUser.userId,
                uploadTime = uploadTime,
                correlationId = correlationId,
            )
        return ResponseEntity.ok(report)
    }

    override fun setQaReportStatus(
        dataPointId: String,
        qaReportId: String,
        statusPatch: QaReportStatusPatch,
    ) {
        val user = DatalandAuthentication.fromContext()
        logger.info(qaLogMessageBuilder.requestChangeQaReportStatus(qaReportId, dataPointId, statusPatch.active))
        dataPointQaReportManager.setQaReportStatus(
            dataPointId = dataPointId,
            qaReportId = qaReportId,
            statusToSet = statusPatch.active,
            requestingUser = user,
        )
    }

    override fun getQaReport(
        dataPointId: String,
        qaReportId: String,
    ): ResponseEntity<DataPointQaReport> {
        val user = DatalandAuthentication.fromContext()
        logger.info(qaLogMessageBuilder.getQaReportMessage(qaReportId, dataPointId))
        qaReportSecurityPolicy.ensureUserCanViewDataPoint(dataPointId, user)
        val report = dataPointQaReportManager.getQaReportById(dataPointId, qaReportId)
        return ResponseEntity.ok(report)
    }

    override fun getAllQaReportsForDataPoint(
        dataPointId: String,
        showInactive: Boolean?,
        reporterUserId: String?,
    ): ResponseEntity<List<DataPointQaReport>> {
        val user = DatalandAuthentication.fromContext()
        qaReportSecurityPolicy.ensureUserCanViewDataPoint(dataPointId, user)
        logger.info(qaLogMessageBuilder.getAllQaReportsForDataIdMessage(dataPointId, reporterUserId))
        val reports =
            dataPointQaReportManager.searchQaReportMetaInfo(
                dataPointId = dataPointId,
                reporterUserId = reporterUserId,
                showInactive = showInactive ?: false,
            )

        return ResponseEntity.ok(reports)
    }
}
