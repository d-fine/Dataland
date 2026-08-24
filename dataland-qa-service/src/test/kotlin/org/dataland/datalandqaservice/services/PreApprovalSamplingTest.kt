package org.dataland.datalandqaservice.services

import org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource
import org.dataland.datalandqaservice.model.reports.QaReportDataPointVerdict
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalConfigPatchRequest
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils.DUMMY_SUBMIT_USER_ID
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils.buildQaReport
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils.buildServiceWithoutLiveDataset
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils.dummyReporter1
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils.runWorkflow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/**
 * Tests that [org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.PreApprovalService.preApproveDataPoints]
 * arrives at the correct [AcceptedDataPointSource] outcome based on the random sampling configuration.
 *
 * See [PreApprovalCheckResultsTest] for tests that verify the diagnostic
 * [org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalCheckResults] fields are
 * populated correctly.
 */
class PreApprovalSamplingTest {
    @Test
    fun `Sampling probability 1 - no datapoints are preapproved`() {
        val service = buildServiceWithoutLiveDataset(autoPreApprovalEnabled = true)
        service.patchConfig(PreApprovalConfigPatchRequest(samplingProbability = 1.0), DUMMY_SUBMIT_USER_ID)
        val reports = listOf(buildQaReport(dummyReporter1, QaReportDataPointVerdict.QaAccepted))

        assertNull(runWorkflow(service, reports))
    }

    @Test
    fun `Sampling probability 0, datapoint is not on exempt list and has report QaAccepted - datapoint gets preapproved`() {
        val service = buildServiceWithoutLiveDataset(autoPreApprovalEnabled = true)
        service.patchConfig(PreApprovalConfigPatchRequest(samplingProbability = 0.0), DUMMY_SUBMIT_USER_ID)
        val reports = listOf(buildQaReport(dummyReporter1, QaReportDataPointVerdict.QaAccepted))

        assertEquals(AcceptedDataPointSource.Original, runWorkflow(service, reports))
    }

    @Test
    fun `getConfig returns samplingProbability`() {
        val service = buildServiceWithoutLiveDataset(autoPreApprovalEnabled = true)
        service.patchConfig(PreApprovalConfigPatchRequest(samplingProbability = 0.42), DUMMY_SUBMIT_USER_ID)

        assertEquals(0.42, service.config.samplingProbability)
    }

    @Test
    fun `patchConfig updates samplingProbability and returns updated config`() {
        val service = buildServiceWithoutLiveDataset(autoPreApprovalEnabled = true)
        val updated =
            service.patchConfig(PreApprovalConfigPatchRequest(samplingProbability = 0.7), DUMMY_SUBMIT_USER_ID)

        assertEquals(0.7, updated.samplingProbability)
        assertEquals(0.7, service.config.samplingProbability)
    }
}
