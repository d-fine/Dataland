package org.dataland.datalandqaservice.services

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource
import org.dataland.datalandqaservice.model.reports.QaReportDataPointVerdict
import org.dataland.datalandqaservice.utils.MockDatasetJudgementEntityForTest
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils.buildDataPointJudgementEntity
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils.buildQaReport
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils.buildServiceWithoutLiveDataset
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils.dummyReporter1
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils.runWorkflow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/**
 * Tests that [org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.PreApprovalService.preApproveDataPoints]
 * arrives at the correct [AcceptedDataPointSource] outcome based on the exempt fields configuration.
 *
 * See [PreApprovalCheckResultsTest] for tests that verify the diagnostic
 * [org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalCheckResults] fields are
 * populated correctly.
 */
class PreApprovalExemptFieldsTest {
    val exemptField = "exempt-field-type"
    val nonExemptField = "non-exempt-field-type"

    @Test
    fun `No preapproval for exempt field even if all reports are QaAccepted`() {
        val service =
            buildServiceWithoutLiveDataset(
                autoPreApprovalEnabled = true,
                exemptFields = mapOf(DataTypeEnum.sfdr to setOf(exemptField)),
            )
        val reports = listOf(buildQaReport(dummyReporter1, QaReportDataPointVerdict.QaAccepted))

        assertNull(runWorkflow(service, reports, dataPointType = exemptField))
    }

    @Test
    fun `Preapproval works for non-exempt field when all reports are QaAccepted`() {
        val service =
            buildServiceWithoutLiveDataset(
                autoPreApprovalEnabled = true,
                exemptFields = mapOf(DataTypeEnum.sfdr to setOf("some-exempt-field-type")),
            )
        val reports = listOf(buildQaReport(dummyReporter1, QaReportDataPointVerdict.QaAccepted))

        assertEquals(AcceptedDataPointSource.Original, runWorkflow(service, reports, dataPointType = nonExemptField))
    }

    @Test
    fun `All qualifying fields are auto-accepted when exempt fields list is empty`() {
        val service =
            buildServiceWithoutLiveDataset(
                autoPreApprovalEnabled = true,
                exemptFields = emptyMap(),
            )
        val reports = listOf(buildQaReport(dummyReporter1, QaReportDataPointVerdict.QaAccepted))

        assertEquals(AcceptedDataPointSource.Original, runWorkflow(service, reports))
    }

    @Test
    fun `Only non-exempt fields are auto-accepted when multiple fields are present`() {
        val service =
            buildServiceWithoutLiveDataset(
                autoPreApprovalEnabled = true,
                exemptFields = mapOf(DataTypeEnum.sfdr to setOf(exemptField)),
            )
        val reports = listOf(buildQaReport(dummyReporter1, QaReportDataPointVerdict.QaAccepted))
        val entity = MockDatasetJudgementEntityForTest.createDummyDatasetJudgementEntity()
        entity.dataPoints.clear()
        entity.dataPoints.add(buildDataPointJudgementEntity(reports, dataPointType = exemptField))
        entity.dataPoints.add(buildDataPointJudgementEntity(reports, dataPointType = nonExemptField))

        val result = service.preApproveDataPoints(entity)

        assertNull(result.dataPoints.first { it.dataPointType == exemptField }.acceptedSource)
        assertEquals(
            AcceptedDataPointSource.Original,
            result.dataPoints.first { it.dataPointType == nonExemptField }.acceptedSource,
        )
    }

    @Test
    fun `Qualifying fields are auto-accepted when exempt fields list contains only non-existent fields`() {
        val service =
            buildServiceWithoutLiveDataset(
                autoPreApprovalEnabled = true,
                exemptFields = mapOf(DataTypeEnum.sfdr to setOf("non-existent-field")),
            )
        val reports = listOf(buildQaReport(dummyReporter1, QaReportDataPointVerdict.QaAccepted))

        assertEquals(AcceptedDataPointSource.Original, runWorkflow(service, reports))
    }

    @Test
    fun `Exempt field in one framework does not block preapproval for the same field in another framework`() {
        val fieldName = "shared-field-type"

        // This test's dataset defaults to DataTypeEnum.sfdr (not lksg), so the lksg-scoped
        // exempt-fields config below should not apply here — verifying exemptions are
        // correctly scoped per framework.

        val service =
            buildServiceWithoutLiveDataset(
                autoPreApprovalEnabled = true,
                exemptFields = mapOf(DataTypeEnum.lksg to setOf(fieldName)),
            )
        val reports = listOf(buildQaReport(dummyReporter1, QaReportDataPointVerdict.QaAccepted))

        assertEquals(AcceptedDataPointSource.Original, runWorkflow(service, reports, dataPointType = fieldName))
    }
}
