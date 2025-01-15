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
        dataId: String,
        qaReport: QaReportDataPoint<String?>,
    ): ResponseEntity<DataPointQaReport> {
        val correlationId = IdUtils.generateUUID()
        val reportingUser = DatalandAuthentication.fromContext()
        logger.info(qaLogMessageBuilder.postQaReportMessage(dataId, reportingUser.userId, correlationId))
        qaReportSecurityPolicy.ensureUserCanViewDataPointForDataId(dataId, reportingUser)
        val uploadTime = Instant.now().toEpochMilli()
        val report =
            dataPointQaReportManager.createQaReport(
                report = qaReport,
                dataId = dataId,
                reporterUserId = reportingUser.userId,
                uploadTime = uploadTime,
                correlationId = correlationId,
            )
        return ResponseEntity.ok(report)
    }

    override fun setQaReportStatus(
        dataId: String,
        qaReportId: String,
        statusPatch: QaReportStatusPatch,
    ) {
        val user = DatalandAuthentication.fromContext()
        logger.info(qaLogMessageBuilder.requestChangeQaReportStatus(qaReportId, dataId, statusPatch.active))
        dataPointQaReportManager.setQaReportStatus(
            dataId = dataId,
            qaReportId = qaReportId,
            statusToSet = statusPatch.active,
            requestingUser = user,
        )
    }

    override fun getQaReport(
        dataId: String,
        qaReportId: String,
    ): ResponseEntity<DataPointQaReport> {
        val user = DatalandAuthentication.fromContext()
        logger.info(qaLogMessageBuilder.getQaReportMessage(qaReportId, dataId))
        qaReportSecurityPolicy.ensureUserCanViewDataPointForDataId(dataId, user)
        val report = dataPointQaReportManager.getQaReportById(dataId, qaReportId)
        return ResponseEntity.ok(report)
    }

    override fun getAllQaReportsForDataPoint(
        dataId: String,
        showInactive: Boolean?,
        reporterUserId: String?,
    ): ResponseEntity<List<DataPointQaReport>> {
        val user = DatalandAuthentication.fromContext()
        qaReportSecurityPolicy.ensureUserCanViewDataPointForDataId(dataId, user)
        logger.info(qaLogMessageBuilder.getAllQaReportsForDataIdMessage(dataId, reporterUserId))
        val reports =
            dataPointQaReportManager.searchQaReportMetaInfo(
                dataId = dataId,
                reporterUserId = reporterUserId,
                showInactive = showInactive ?: false,
            )

        return ResponseEntity.ok(reports)
    }
}
