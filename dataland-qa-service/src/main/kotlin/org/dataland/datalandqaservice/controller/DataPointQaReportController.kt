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
        qaReportSecurityPolicy.ensureUserCanViewDataPointQaReportForDataId(dataId, reportingUser)
        val uploadTime = Instant.now().toEpochMilli()
        val reportEntity =
            dataPointQaReportManager.createQaReport(
                report = qaReport,
                dataId = dataId,
                reporterUserId = reportingUser.userId,
                uploadTime = uploadTime,
                correlationId = correlationId,
            )
        return ResponseEntity.ok(reportEntity.toApiModel())
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
        qaReportSecurityPolicy.ensureUserCanViewDataPointQaReportForDataId(dataId, user)
        val reportEntity = dataPointQaReportManager.getQaReportById(dataId, qaReportId)
        return ResponseEntity.ok(reportEntity.toApiModel())
    }

    override fun getAllQaReportsForDataset(
        dataId: String,
        showInactive: Boolean?,
        reporterUserId: String?,
    ): ResponseEntity<List<DataPointQaReport>> {
        val user = DatalandAuthentication.fromContext()
        qaReportSecurityPolicy.ensureUserCanViewDataPointQaReportForDataId(dataId, user)
        logger.info(qaLogMessageBuilder.getAllQaReportsForDataIdMessage(dataId, reporterUserId))
        val reportEntities =
            dataPointQaReportManager.searchQaReportMetaInfo(
                dataId = dataId,
                reporterUserId = reporterUserId,
                showInactive = showInactive ?: false,
            )
        val apiModel =
            reportEntities.map {
                it.toApiModel()
            }

        return ResponseEntity.ok(apiModel)
    }
}
