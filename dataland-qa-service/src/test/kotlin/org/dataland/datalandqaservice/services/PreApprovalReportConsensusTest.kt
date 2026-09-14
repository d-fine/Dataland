package org.dataland.datalandqaservice.services

import org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource
import org.dataland.datalandqaservice.model.reports.QaReportDataPointVerdict
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils.buildQaReport
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils.buildServiceWithoutLiveDataset
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils.dummyReporter1
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils.dummyReporter2
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils.runWorkflow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/**
 * Tests that [org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.PreApprovalService.preApproveDataPoints]
 * arrives at the correct [AcceptedDataPointSource] outcome based on the consensus of QA reports.
 *
 * See [PreApprovalCheckResultsTest] for tests that verify the diagnostic
 * [org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalCheckResults] fields are
 * populated correctly.
 */
class PreApprovalReportConsensusTest {
    @Test
    fun `No preapproval when environment variable is set to false`() {
        val service = buildServiceWithoutLiveDataset(autoPreApprovalEnabled = false)
        val reports = listOf(buildQaReport(dummyReporter1, QaReportDataPointVerdict.QaAccepted))

        assertNull(runWorkflow(service, reports))
    }

    @Test
    fun `Preapproval works when environment variable is true, there is only 1 reporter and report is QaAccepted`() {
        val service = buildServiceWithoutLiveDataset(autoPreApprovalEnabled = true)
        val reports = listOf(buildQaReport(dummyReporter1, QaReportDataPointVerdict.QaAccepted))

        assertEquals(AcceptedDataPointSource.Original, runWorkflow(service, reports))
    }

    @Test
    fun `Preapproval works when environment variable is true, there are 2 reporter and all reports are QaAccepted`() {
        val service = buildServiceWithoutLiveDataset(autoPreApprovalEnabled = true)
        val reports =
            listOf(
                buildQaReport(dummyReporter1, QaReportDataPointVerdict.QaAccepted),
                buildQaReport(dummyReporter2, QaReportDataPointVerdict.QaAccepted),
            )

        assertEquals(AcceptedDataPointSource.Original, runWorkflow(service, reports))
    }

    @Test
    fun `No preapproval when there are two reports with mixed verdicts`() {
        val service = buildServiceWithoutLiveDataset(autoPreApprovalEnabled = true)
        val reports =
            listOf(
                buildQaReport(dummyReporter1, QaReportDataPointVerdict.QaAccepted),
                buildQaReport(dummyReporter2, QaReportDataPointVerdict.QaRejected),
            )

        assertNull(runWorkflow(service, reports))
    }

    @Test
    fun `No preapproval when there are no QA reports`() {
        val service = buildServiceWithoutLiveDataset(autoPreApprovalEnabled = true)

        assertNull(runWorkflow(service, emptyList()))
    }
}
