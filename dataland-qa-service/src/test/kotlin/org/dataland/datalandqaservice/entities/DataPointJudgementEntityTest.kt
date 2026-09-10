package org.dataland.datalandqaservice.entities

import org.assertj.core.api.Assertions.assertThat
import org.dataland.datalandqaservice.model.reports.QaReportDataPointVerdict
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointJudgementEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointQaReportEntity
import org.junit.jupiter.api.Test
import java.util.UUID

class   DataPointJudgementEntityTest {
    private val dataPointType = "dummy-datapoint-type"
    private val dataPointId = "dummy-datapoint-id"
    private val reporterA = UUID.randomUUID().toString()
    private val reporterB = UUID.randomUUID().toString()

    private fun buildQaReport(
        reporterUserId: String,
        uploadTime: Long,
        verdict: QaReportDataPointVerdict,
    ): DataPointQaReportEntity =
        DataPointQaReportEntity(
            qaReportId = UUID.randomUUID().toString(),
            comment = "",
            verdict = verdict,
            correctedData = null,
            dataPointId = dataPointId,
            dataPointType = dataPointType,
            reporterUserId = reporterUserId,
            uploadTime = uploadTime,
            active = true,
        )

    @Test
    fun `toDataPointJudgementDetails only includes the latest QA report per reviewer`() {
        val reporterAOldestRejected = buildQaReport(reporterA, uploadTime = 100L, verdict = QaReportDataPointVerdict.QaRejected)
        val reporterALatestAccepted = buildQaReport(reporterA, uploadTime = 300L, verdict = QaReportDataPointVerdict.QaAccepted)
        val reporterAMiddleRejected = buildQaReport(reporterA, uploadTime = 200L, verdict = QaReportDataPointVerdict.QaRejected)
        val reporterBOnlyReport = buildQaReport(reporterB, uploadTime = 150L, verdict = QaReportDataPointVerdict.QaAccepted)

        val entity =
            DataPointJudgementEntity(
                dataPointType = dataPointType,
                dataPointId = dataPointId,
                qaReports =
                    mutableListOf(
                        reporterAOldestRejected,
                        reporterALatestAccepted,
                        reporterAMiddleRejected,
                        reporterBOnlyReport,
                    ),
                acceptedSource = null,
                reporterUserIdOfAcceptedQaReport = null,
                customValue = null,
            )

        val resultingQaReports = entity.toDataPointJudgementDetails().qaReports

        assertThat(resultingQaReports).hasSize(2)

        val reporterAResult = resultingQaReports.single { it.reporterUserId == reporterA }
        assertThat(reporterAResult.uploadTime).isEqualTo(300L)
        assertThat(reporterAResult.verdict).isEqualTo(QaReportDataPointVerdict.QaAccepted)

        val reporterBResult = resultingQaReports.single { it.reporterUserId == reporterB }
        assertThat(reporterBResult.uploadTime).isEqualTo(150L)
        assertThat(reporterBResult.verdict).isEqualTo(QaReportDataPointVerdict.QaAccepted)

        assertThat(entity.qaReports).hasSize(4)
    }
}
