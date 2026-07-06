package org.dataland.datalandqaservice.services

import com.fasterxml.jackson.databind.node.DecimalNode
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandqaservice.configurations.PreApprovalExemptFieldsConfig
import org.dataland.datalandqaservice.model.reports.QaReportDataPointVerdict
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointQaReportEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalCheckResults
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalConfig
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.PreApprovalService
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.SignificanceCheckService
import org.dataland.datalandqaservice.utils.MockDatasetJudgementEntityForTest
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils.buildDataPointJudgementEntity
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils.buildQaReport
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils.buildServiceWithLiveDatasetForSignificanceCheck
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils.buildServiceWithoutLiveDataset
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils.dummyReporter1
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils.dummyReporter2
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.util.UUID

/**
 * Tests that [PreApprovalService.preApproveDataPoints] populates the diagnostic [PreApprovalCheckResults]
 * fields correctly for every pre-approval rule (report consensus, exempt fields, sampling, and significance
 * checks).
 *
 * See [PreApprovalServiceTest] for tests that verify the resulting `acceptedSource` outcome itself.
 */
class PreApprovalCheckResultsTest {
    fun checkPreApprovalCheckResultsField(autoPreApprovalEnabled: Boolean) {
        val service = buildServiceWithoutLiveDataset(autoPreApprovalEnabled = autoPreApprovalEnabled)
        val entity = MockDatasetJudgementEntityForTest.createDummyDatasetJudgementEntity()
        val preApprovedDataPoints = service.preApproveDataPoints(entity)
        preApprovedDataPoints.dataPoints.forEach { dataPoint ->
            if (autoPreApprovalEnabled) {
                assertNotNull(dataPoint.preApprovalCheckResults)
            } else {
                assertNull(dataPoint.preApprovalCheckResults)
            }
        }
    }

    private fun runWorkflowAndReturnCheckResults(
        service: PreApprovalService,
        reports: List<DataPointQaReportEntity>,
        dataPointType: String = MockDatasetJudgementEntityForTest.DUMMY_DATA_POINT_TYPE,
        dataPointId: String = UUID.randomUUID().toString(),
    ): PreApprovalCheckResults? =
        service
            .preApproveDataPoints(
                MockDatasetJudgementEntityForTest.createDummyDatasetJudgementEntity().also { entity ->
                    entity.dataPoints.clear()
                    entity.dataPoints.add(
                        buildDataPointJudgementEntity(
                            qaReports = reports,
                            dataPointType = dataPointType,
                            dataPointId = dataPointId,
                        ),
                    )
                },
            ).dataPoints
            .first()
            .preApprovalCheckResults

    @Test
    fun `pre-approval check results are populated`() {
        checkPreApprovalCheckResultsField(autoPreApprovalEnabled = true)
    }

    @Test
    fun `pre-approval check results is null if pre-approval flag is off`() {
        checkPreApprovalCheckResultsField(autoPreApprovalEnabled = false)
    }

    @Test
    fun `pre-approval check results stores whether all QA reports are accepted`() {
        val service = buildServiceWithoutLiveDataset(autoPreApprovalEnabled = true)
        service.patchConfig(PreApprovalConfig(samplingProbability = 0.0))

        // Scenario 1: two QA-reports, one rejected, one accepted -> should lead to false
        var reports =
            listOf(
                buildQaReport(dummyReporter1, QaReportDataPointVerdict.QaRejected),
                buildQaReport(dummyReporter2, QaReportDataPointVerdict.QaAccepted),
            )

        var checkResults = requireNotNull(runWorkflowAndReturnCheckResults(service, reports))

        assertEquals(false, checkResults.areAllQaReportsAccepted)

        // Scenario 2: two QA-reports, two accepted -> should lead to true
        reports =
            listOf(
                buildQaReport(dummyReporter1, QaReportDataPointVerdict.QaAccepted),
                buildQaReport(dummyReporter2, QaReportDataPointVerdict.QaAccepted),
            )

        checkResults = requireNotNull(runWorkflowAndReturnCheckResults(service, reports))

        assertEquals(true, checkResults.areAllQaReportsAccepted)

        // Scenario 3: no QA-report -> should lead to false
        reports = emptyList()

        checkResults = requireNotNull(runWorkflowAndReturnCheckResults(service, reports))

        assertEquals(false, checkResults.areAllQaReportsAccepted)
    }

    @Test
    fun `pre-approval check results stores whether datapoint is eligible`() {
        val exemptField = "exempt-field-type"
        val nonExemptField = "non-exempt-field-type"
        val service =
            buildServiceWithoutLiveDataset(
                autoPreApprovalEnabled = true,
                exemptFieldsConfig = PreApprovalExemptFieldsConfig(mapOf(DataTypeEnum.sfdr to setOf(exemptField))),
            )
        service.patchConfig(PreApprovalConfig(samplingProbability = 0.0))

        // Scenario 1: data point type is on the exempt field list
        var checkResults = requireNotNull(runWorkflowAndReturnCheckResults(service, emptyList(), dataPointType = exemptField))
        assertEquals(false, checkResults.dataPointEligible)

        // Scenario 2: data point type is not on the exempt field list
        checkResults = requireNotNull(runWorkflowAndReturnCheckResults(service, emptyList(), dataPointType = nonExemptField))
        assertEquals(true, checkResults.dataPointEligible)
    }

    @Test
    fun `pre-approval check results stores whether datapoint passes random sampling`() {
        val service = buildServiceWithoutLiveDataset(autoPreApprovalEnabled = true)

        // Scenario 1: 100% probability of being sampled and therefore not preapproved
        service.patchConfig(PreApprovalConfig(samplingProbability = 1.0))
        var checkResults = requireNotNull(runWorkflowAndReturnCheckResults(service, emptyList()))
        assertEquals(false, checkResults.passesRandomSampling)

        // Scenario 2: 0% probability of being sampled and therefore preapproved
        service.patchConfig(PreApprovalConfig(samplingProbability = 0.0))
        checkResults = requireNotNull(runWorkflowAndReturnCheckResults(service, emptyList()))
        assertEquals(true, checkResults.passesRandomSampling)
    }

    @Test
    fun `pre-approval check results stores whether datapoint passes significance check`() {
        val dataPointType = "extendedDecimalField"
        val originalDataPointId = UUID.randomUUID().toString()
        val liveDataPointId = UUID.randomUUID().toString()
        val originalValue = BigDecimal.valueOf(200)
        val liveValue = BigDecimal.valueOf(100)

        for (hasSignificantChange in listOf(false, true)) {
            val mockedSignificanceCheckService = mock<SignificanceCheckService>()
            whenever {
                mockedSignificanceCheckService.hasSignificantChange(
                    any(),
                    any(),
                    anyOrNull(),
                    any(),
                    any(),
                )
            } doReturn (hasSignificantChange)

            val service =
                buildServiceWithLiveDatasetForSignificanceCheck(
                    originalDataPointId = originalDataPointId,
                    liveDataPointId = liveDataPointId,
                    originalValueNode = DecimalNode(originalValue),
                    liveValueNode = DecimalNode(liveValue),
                    baseTypeId = "extendedDecimal",
                    dpType = dataPointType,
                    significanceCheckService = mockedSignificanceCheckService,
                )

            val checkResults =
                requireNotNull(
                    runWorkflowAndReturnCheckResults(
                        service = service,
                        reports = emptyList(),
                        dataPointType = dataPointType,
                        dataPointId = originalDataPointId,
                    ),
                )
            assertEquals(!hasSignificantChange, checkResults.passesSignificanceCheck)
        }
    }
}
