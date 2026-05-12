package org.dataland.datalandqaservice.services

import org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource
import org.dataland.datalandqaservice.model.reports.QaReportDataPointVerdict
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointJudgementEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointQaReportEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.PreApprovalService
import org.dataland.datalandqaservice.utils.MockDatasetJudgementEntityForTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.UUID

class PreApprovalServiceTest {
    private val dummyReporter1 = UUID.randomUUID().toString()
    private val dummyReporter2 = UUID.randomUUID().toString()

    private fun buildQaReport(
        reporterUserId: String,
        verdict: QaReportDataPointVerdict,
        uploadTime: Long = 1000L,
        active: Boolean = true,
        dataPointId: String = UUID.randomUUID().toString(),
    ): DataPointQaReportEntity =
        DataPointQaReportEntity(
            qaReportId = UUID.randomUUID().toString(),
            comment = "",
            verdict = verdict,
            correctedData = null,
            dataPointId = dataPointId,
            dataPointType = MockDatasetJudgementEntityForTest.DUMMY_DATA_POINT_TYPE,
            reporterUserId = reporterUserId,
            uploadTime = uploadTime,
            active = active,
        )

    private fun buildDataPointJudgementEntity(qaReports: List<DataPointQaReportEntity>): DataPointJudgementEntity =
        DataPointJudgementEntity(
            dataPointType = MockDatasetJudgementEntityForTest.DUMMY_DATA_POINT_TYPE,
            dataPointId = UUID.randomUUID().toString(),
            qaReports = qaReports.toMutableList(),
            acceptedSource = null,
            reporterUserIdOfAcceptedQaReport = null,
            customValue = null,
        )

    private fun runWorkflow(
        service: PreApprovalService,
        reports: List<DataPointQaReportEntity>,
    ): AcceptedDataPointSource? {
        val dataPoint = buildDataPointJudgementEntity(reports)
        val entity = MockDatasetJudgementEntityForTest.createDummyDatasetJudgementEntity()
        entity.dataPoints.clear()
        entity.dataPoints.add(dataPoint)
        return service
            .runPreApprovalWorkflow(entity)
            .dataPoints
            .first()
            .acceptedSource
    }

    @Test
    fun `No preapproval when environment variable is set to false`() {
        val service = PreApprovalService(autoPreApprovalEnabled = false)
        val entity = MockDatasetJudgementEntityForTest.createDummyDatasetJudgementEntity()

        val result = service.runPreApprovalWorkflow(entity)

        assertNull(result.dataPoints.first().acceptedSource)
    }

    @Test
    fun `Preapproval works when environment variable is true, there is only 1 reporter and report is QaAccepted`() {
        val service = PreApprovalService(autoPreApprovalEnabled = true)
        val reports = listOf(buildQaReport(dummyReporter1, QaReportDataPointVerdict.QaAccepted))

        assertEquals(AcceptedDataPointSource.Original, runWorkflow(service, reports))
    }

    @Test
    fun `Preapproval works when environment variable is true, there are 2 reporter and all reports are QaAccepted`() {
        val service = PreApprovalService(autoPreApprovalEnabled = true)
        val reports =
            listOf(
                buildQaReport(dummyReporter1, QaReportDataPointVerdict.QaAccepted),
                buildQaReport(dummyReporter2, QaReportDataPointVerdict.QaAccepted),
            )

        assertEquals(AcceptedDataPointSource.Original, runWorkflow(service, reports))
    }

    @Test
    fun `No preapproval when there are two reports with mixed verdicts`() {
        val service = PreApprovalService(autoPreApprovalEnabled = true)
        val reports =
            listOf(
                buildQaReport(dummyReporter1, QaReportDataPointVerdict.QaAccepted),
                buildQaReport(dummyReporter2, QaReportDataPointVerdict.QaRejected),
            )

        assertNull(runWorkflow(service, reports))
    }

    @Test fun `No preapproval when there are no QA reports`() {
        val service = PreApprovalService(autoPreApprovalEnabled = true)

        assertNull(runWorkflow(service, emptyList()))
    }

    @Test
    fun `Preapproval works when there are multiple reports per reporter and the latest is QaAccepted`() {
        val service = PreApprovalService(autoPreApprovalEnabled = true)
        val reports =
            listOf(
                buildQaReport(dummyReporter1, QaReportDataPointVerdict.QaRejected, uploadTime = 1000L),
                buildQaReport(dummyReporter1, QaReportDataPointVerdict.QaAccepted, uploadTime = 2000L),
            )

        assertEquals(AcceptedDataPointSource.Original, runWorkflow(service, reports))
    }

    @Test
    fun `No preapproval when there are multiple reports per reporter and the latest is QaRejected`() {
        val service = PreApprovalService(autoPreApprovalEnabled = true)
        val reports =
            listOf(
                buildQaReport(dummyReporter1, QaReportDataPointVerdict.QaAccepted, uploadTime = 1000L),
                buildQaReport(dummyReporter1, QaReportDataPointVerdict.QaRejected, uploadTime = 2000L),
            )

        assertNull(runWorkflow(service, reports))
    }
}
