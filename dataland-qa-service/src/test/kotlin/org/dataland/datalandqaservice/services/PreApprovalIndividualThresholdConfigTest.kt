package org.dataland.datalandqaservice.services

import com.fasterxml.jackson.databind.node.DecimalNode
import com.fasterxml.jackson.databind.node.IntNode
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource
import org.dataland.datalandqaservice.model.reports.QaReportDataPointVerdict
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalConfigPatchRequest
import org.dataland.datalandqaservice.utils.MockDatasetJudgementEntityForTest
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils.DUMMY_SUBMIT_USER_ID
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils.buildDataPointJudgementEntity
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils.buildQaReport
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils.buildServiceWithLiveDatasetForSignificanceCheck
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils.dummyReporter1
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID

/**
 * Tests that individual per-data-point-type threshold overrides configured via
 * [PreApprovalConfigPatchRequest.individualDecimalThresholds] and
 * [PreApprovalConfigPatchRequest.individualIntegerThresholds] take effect over the global thresholds, and
 * that the global thresholds are used as a fallback when no override exists.
 */
class PreApprovalIndividualThresholdConfigTest {
    @Test
    fun `individual decimal threshold override from config takes effect over the global threshold`() {
        val dataPointType = "extendedDecimalField"
        val originalDataPointId = UUID.randomUUID().toString()
        val liveDataPointId = UUID.randomUUID().toString()
        val service =
            buildServiceWithLiveDatasetForSignificanceCheck(
                originalDataPointId = originalDataPointId,
                liveDataPointId = liveDataPointId,
                originalValueNode = DecimalNode(BigDecimal.valueOf(110)),
                liveValueNode = DecimalNode(BigDecimal.valueOf(100)),
                baseTypeId = "extendedDecimal",
                dpType = dataPointType,
            )
        // A 10% change is not significant against the (default) global threshold of 0.5, but is
        // significant against a lower individual override of 0.05 for this specific data point.
        service.patchConfig(
            PreApprovalConfigPatchRequest(
                individualDecimalThresholds = mapOf(DataTypeEnum.sfdr to mapOf(dataPointType to 0.05)),
            ),
            DUMMY_SUBMIT_USER_ID,
        )
        val reports = listOf(buildQaReport(dummyReporter1, QaReportDataPointVerdict.QaAccepted))
        val dataPoint =
            buildDataPointJudgementEntity(
                qaReports = reports,
                dataPointType = dataPointType,
                dataPointId = originalDataPointId,
            )
        val entity = MockDatasetJudgementEntityForTest.createDummyDatasetJudgementEntity()
        entity.dataPoints.clear()
        entity.dataPoints.add(dataPoint)

        val result =
            service
                .preApproveDataPoints(entity)
                .dataPoints
                .first()
                .acceptedSource

        assertNull(result)
    }

    @Test
    fun `individual integer threshold falls back to the global threshold when no override exists`() {
        val dataPointType = "extendedIntegerField"
        val originalDataPointId = UUID.randomUUID().toString()
        val liveDataPointId = UUID.randomUUID().toString()
        val service =
            buildServiceWithLiveDatasetForSignificanceCheck(
                originalDataPointId = originalDataPointId,
                liveDataPointId = liveDataPointId,
                originalValueNode = IntNode(7),
                liveValueNode = IntNode(5),
                baseTypeId = "extendedInteger",
                dpType = dataPointType,
            )
        // No individual override is configured for this data point type, so the (default) global
        // integer threshold of 5 applies, under which a change of 2 is not significant.
        service.patchConfig(
            PreApprovalConfigPatchRequest(
                individualIntegerThresholds =
                    mapOf(DataTypeEnum.sfdr to mapOf("some-other-field" to 1L)),
            ),
            DUMMY_SUBMIT_USER_ID,
        )
        val reports = listOf(buildQaReport(dummyReporter1, QaReportDataPointVerdict.QaAccepted))
        val dataPoint =
            buildDataPointJudgementEntity(
                qaReports = reports,
                dataPointType = dataPointType,
                dataPointId = originalDataPointId,
            )
        val entity = MockDatasetJudgementEntityForTest.createDummyDatasetJudgementEntity()
        entity.dataPoints.clear()
        entity.dataPoints.add(dataPoint)

        val result =
            service
                .preApproveDataPoints(entity)
                .dataPoints
                .first()
                .acceptedSource

        assertEquals(AcceptedDataPointSource.Original, result)
    }
}
