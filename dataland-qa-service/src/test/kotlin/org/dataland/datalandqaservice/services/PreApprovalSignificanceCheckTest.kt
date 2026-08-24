package org.dataland.datalandqaservice.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.DecimalNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.TextNode
import org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource
import org.dataland.datalandqaservice.model.reports.QaReportDataPointVerdict
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.PreApprovalService
import org.dataland.datalandqaservice.utils.MockDatasetJudgementEntityForTest
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils.buildDataPointJudgementEntity
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils.buildQaReport
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils.buildServiceWithLiveDatasetForSignificanceCheck
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils.buildServiceWithoutLiveDataset
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils.dummyReporter1
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils.runWorkflow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID

/**
 * Tests that [PreApprovalService.preApproveDataPoints] arrives at the correct [AcceptedDataPointSource]
 * outcome based on the significance check against the currently live dataset.
 *
 * See [PreApprovalCheckResultsTest] for tests that verify the diagnostic
 * [org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalCheckResults] fields are
 * populated correctly.
 */
class PreApprovalSignificanceCheckTest {
    private val dataPointType = "extendedDecimalField"
    private val originalDataPointId = UUID.randomUUID().toString()
    private val liveDataPointId = UUID.randomUUID().toString()

    private fun buildServiceWithLiveDataset(
        originalValueNode: JsonNode?,
        liveValueNode: JsonNode?,
        baseTypeId: String,
        dpType: String = dataPointType,
        liveDataPointMap: Map<String, String> = mapOf(dpType to liveDataPointId),
    ): PreApprovalService =
        buildServiceWithLiveDatasetForSignificanceCheck(
            originalDataPointId = originalDataPointId,
            liveDataPointId = liveDataPointId,
            originalValueNode = originalValueNode,
            liveValueNode = liveValueNode,
            baseTypeId = baseTypeId,
            dpType = dpType,
            liveDataPointMap = liveDataPointMap,
        )

    private fun runSignificanceWorkflow(
        service: PreApprovalService,
        dpType: String = dataPointType,
    ): AcceptedDataPointSource? {
        val reports = listOf(buildQaReport(dummyReporter1, QaReportDataPointVerdict.QaAccepted))
        val dataPoint =
            buildDataPointJudgementEntity(
                qaReports = reports,
                dataPointType = dpType,
                dataPointId = originalDataPointId,
            )
        val entity = MockDatasetJudgementEntityForTest.createDummyDatasetJudgementEntity()
        entity.dataPoints.clear()
        entity.dataPoints.add(dataPoint)
        return service
            .preApproveDataPoints(entity)
            .dataPoints
            .first()
            .acceptedSource
    }

    @Test
    fun `no live dataset - preapproval proceeds as normal`() {
        val service = buildServiceWithoutLiveDataset(autoPreApprovalEnabled = true)
        val reports = listOf(buildQaReport(dummyReporter1, QaReportDataPointVerdict.QaAccepted))

        assertEquals(AcceptedDataPointSource.Original, runWorkflow(service, reports))
    }

    @Test
    fun `significant decimal change suppresses preapproval`() {
        val service =
            buildServiceWithLiveDataset(
                originalValueNode = DecimalNode(BigDecimal.valueOf(200)),
                liveValueNode = DecimalNode(BigDecimal.valueOf(100)),
                baseTypeId = "extendedDecimal",
            )

        assertNull(runSignificanceWorkflow(service))
    }

    @Test
    fun `non-significant decimal change allows preapproval`() {
        val service =
            buildServiceWithLiveDataset(
                originalValueNode = DecimalNode(BigDecimal.valueOf(110)),
                liveValueNode = DecimalNode(BigDecimal.valueOf(100)),
                baseTypeId = "extendedDecimal",
            )

        assertEquals(AcceptedDataPointSource.Original, runSignificanceWorkflow(service))
    }

    @Test
    fun `significant integer change suppresses preapproval`() {
        val service =
            buildServiceWithLiveDataset(
                originalValueNode = IntNode(15),
                liveValueNode = IntNode(5),
                baseTypeId = "extendedInteger",
            )

        assertNull(runSignificanceWorkflow(service))
    }

    @Test
    fun `non-significant integer change allows preapproval`() {
        val service =
            buildServiceWithLiveDataset(
                originalValueNode = IntNode(7),
                liveValueNode = IntNode(5),
                baseTypeId = "extendedInteger",
            )

        assertEquals(AcceptedDataPointSource.Original, runSignificanceWorkflow(service))
    }

    @Test
    fun `significant boolean change suppresses preapproval`() {
        val service =
            buildServiceWithLiveDataset(
                originalValueNode = TextNode("Yes"),
                liveValueNode = TextNode("No"),
                baseTypeId = "extendedEnumYesNo",
            )

        assertNull(runSignificanceWorkflow(service))
    }

    @Test
    fun `non-significant boolean (same value) allows preapproval`() {
        val service =
            buildServiceWithLiveDataset(
                originalValueNode = TextNode("Yes"),
                liveValueNode = TextNode("Yes"),
                baseTypeId = "extendedEnumYesNo",
            )

        assertEquals(AcceptedDataPointSource.Original, runSignificanceWorkflow(service))
    }

    @Test
    fun `original non-null but live value null allows preapproval`() {
        val service =
            buildServiceWithLiveDataset(
                originalValueNode = DecimalNode(BigDecimal.valueOf(100)),
                liveValueNode = NullNode.instance,
                baseTypeId = "extendedDecimal",
            )

        assertEquals(AcceptedDataPointSource.Original, runSignificanceWorkflow(service))
    }

    @Test
    fun `datapoint type not present in live dataset allows preapproval`() {
        val service =
            buildServiceWithLiveDataset(
                originalValueNode = DecimalNode(BigDecimal.valueOf(100)),
                liveValueNode = DecimalNode(BigDecimal.valueOf(200)),
                baseTypeId = "extendedDecimal",
                liveDataPointMap = emptyMap(),
            )

        assertEquals(AcceptedDataPointSource.Original, runSignificanceWorkflow(service))
    }
}
