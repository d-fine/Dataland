package org.dataland.datalandqaservice.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.DecimalNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.TextNode
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandqaservice.configurations.PreApprovalExemptFieldsConfig
import org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource
import org.dataland.datalandqaservice.model.reports.QaReportDataPointVerdict
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalConfig
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.PreApprovalService
import org.dataland.datalandqaservice.utils.MockDatasetJudgementEntityForTest
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils.buildDataPointJudgementEntity
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils.buildQaReport
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils.buildServiceWithLiveDatasetForSignificanceCheck
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils.buildServiceWithoutLiveDataset
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils.dummyReporter1
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils.dummyReporter2
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils.runWorkflow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID

/**
 * Tests that [PreApprovalService.preApproveDataPoints] arrives at the correct [AcceptedDataPointSource]
 * outcome for every pre-approval rule (report consensus, exempt fields, sampling, and significance checks).
 *
 * See [org.dataland.datalandqaservice.services.PreApprovalCheckResultsTest] for tests that verify the
 * diagnostic [org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalCheckResults]
 * fields are populated correctly.
 */
class PreApprovalServiceTest {
    @Nested
    inner class ReportConsensusTests {
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

    @Nested
    inner class ExemptFieldsTests {
        val exemptField = "exempt-field-type"
        val nonExemptField = "non-exempt-field-type"

        @Test
        fun `No preapproval for exempt field even if all reports are QaAccepted`() {
            val service =
                buildServiceWithoutLiveDataset(
                    autoPreApprovalEnabled = true,
                    exemptFieldsConfig = PreApprovalExemptFieldsConfig(mapOf(DataTypeEnum.sfdr to setOf(exemptField))),
                )
            val reports = listOf(buildQaReport(dummyReporter1, QaReportDataPointVerdict.QaAccepted))

            assertNull(runWorkflow(service, reports, dataPointType = exemptField))
        }

        @Test
        fun `Preapproval works for non-exempt field when all reports are QaAccepted`() {
            val service =
                buildServiceWithoutLiveDataset(
                    autoPreApprovalEnabled = true,
                    exemptFieldsConfig = PreApprovalExemptFieldsConfig(mapOf(DataTypeEnum.sfdr to setOf("some-exempt-field-type"))),
                )
            val reports = listOf(buildQaReport(dummyReporter1, QaReportDataPointVerdict.QaAccepted))

            assertEquals(AcceptedDataPointSource.Original, runWorkflow(service, reports, dataPointType = nonExemptField))
        }

        @Test
        fun `All qualifying fields are auto-accepted when exempt fields list is empty`() {
            val service =
                buildServiceWithoutLiveDataset(
                    autoPreApprovalEnabled = true,
                    exemptFieldsConfig = PreApprovalExemptFieldsConfig(emptyMap()),
                )
            val reports = listOf(buildQaReport(dummyReporter1, QaReportDataPointVerdict.QaAccepted))

            assertEquals(AcceptedDataPointSource.Original, runWorkflow(service, reports))
        }

        @Test
        fun `Only non-exempt fields are auto-accepted when multiple fields are present`() {
            val service =
                buildServiceWithoutLiveDataset(
                    autoPreApprovalEnabled = true,
                    exemptFieldsConfig = PreApprovalExemptFieldsConfig(mapOf(DataTypeEnum.sfdr to setOf(exemptField))),
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
                    exemptFieldsConfig = PreApprovalExemptFieldsConfig(mapOf(DataTypeEnum.sfdr to setOf("non-existent-field"))),
                )
            val reports = listOf(buildQaReport(dummyReporter1, QaReportDataPointVerdict.QaAccepted))

            assertEquals(AcceptedDataPointSource.Original, runWorkflow(service, reports))
        }

        @Test
        fun `Exempt field in one framework does not block preapproval for the same field in another framework`() {
            val fieldName = "shared-field-type"
            val service =
                buildServiceWithoutLiveDataset(
                    autoPreApprovalEnabled = true,
                    exemptFieldsConfig = PreApprovalExemptFieldsConfig(mapOf(DataTypeEnum.lksg to setOf(fieldName))),
                )
            val reports = listOf(buildQaReport(dummyReporter1, QaReportDataPointVerdict.QaAccepted))

            assertEquals(AcceptedDataPointSource.Original, runWorkflow(service, reports, dataPointType = fieldName))
        }
    }

    @Nested
    inner class SamplingTests {
        @Test
        fun `Sampling probability 1 - no datapoints are preapproved`() {
            val service = buildServiceWithoutLiveDataset(autoPreApprovalEnabled = true)
            service.patchConfig(PreApprovalConfig(samplingProbability = 1.0))
            val reports = listOf(buildQaReport(dummyReporter1, QaReportDataPointVerdict.QaAccepted))

            assertNull(runWorkflow(service, reports))
        }

        @Test
        fun `Sampling probability 0, datapoint is not on exempt list and has report QaAccepted - datapoint gets preapproved`() {
            val service = buildServiceWithoutLiveDataset(autoPreApprovalEnabled = true)
            service.patchConfig(PreApprovalConfig(samplingProbability = 0.0))
            val reports = listOf(buildQaReport(dummyReporter1, QaReportDataPointVerdict.QaAccepted))

            assertEquals(AcceptedDataPointSource.Original, runWorkflow(service, reports))
        }

        @Test
        fun `getConfig returns samplingProbability`() {
            val service = buildServiceWithoutLiveDataset(autoPreApprovalEnabled = true)
            service.patchConfig(PreApprovalConfig(samplingProbability = 0.42))

            assertEquals(0.42, service.config.samplingProbability)
        }

        @Test
        fun `patchConfig updates samplingProbability and returns updated config`() {
            val service = buildServiceWithoutLiveDataset(autoPreApprovalEnabled = true)
            val updated = service.patchConfig(PreApprovalConfig(samplingProbability = 0.7))

            assertEquals(0.7, updated.samplingProbability)
            assertEquals(0.7, service.config.samplingProbability)
        }
    }

    @Nested
    inner class SignificanceCheckTests {
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
}
