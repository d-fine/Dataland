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
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointJudgementEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointQaReportEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalConfig
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DatasetJudgementSupportService
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.PreApprovalService
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.SignificanceCheckService
import org.dataland.datalandqaservice.utils.MockDatasetJudgementEntityForTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.util.UUID

class PreApprovalServiceTest {
    private val dummyReporter1 = UUID.randomUUID().toString()
    private val dummyReporter2 = UUID.randomUUID().toString()
    private val significanceCheckService = SignificanceCheckService()

    private fun buildQaReport(
        reporterUserId: String,
        verdict: QaReportDataPointVerdict,
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
            uploadTime = 1000L,
            active = true,
        )

    private fun buildDataPointJudgementEntity(
        qaReports: List<DataPointQaReportEntity>,
        dataPointType: String = MockDatasetJudgementEntityForTest.DUMMY_DATA_POINT_TYPE,
        dataPointId: String = UUID.randomUUID().toString(),
    ): DataPointJudgementEntity =
        DataPointJudgementEntity(
            dataPointType = dataPointType,
            dataPointId = dataPointId,
            qaReports = qaReports.toMutableList(),
            acceptedSource = null,
            reporterUserIdOfAcceptedQaReport = null,
            customValue = null,
            reasonForCustomDataPoint = null,
        )

    /**
     * Creates a mock [DatasetJudgementSupportService] that returns no live dataset.
     * Use this for tests that cover pre-existing behaviour unrelated to significance checks.
     */
    private fun mockSupportServiceWithNoLiveDataset(): DatasetJudgementSupportService =
        mock<DatasetJudgementSupportService>().also {
            whenever(it.getDataPointsOfLatestActiveDataset(any(), any())).thenReturn(null)
        }

    /**
     * Builds a [PreApprovalService] pre-configured to skip the significance check
     * (no live dataset). Use this for all tests that verify pre-existing behaviour.
     */
    private fun buildServiceWithoutLiveDataset(
        autoPreApprovalEnabled: Boolean,
        exemptFieldsConfig: PreApprovalExemptFieldsConfig = PreApprovalExemptFieldsConfig(),
    ): PreApprovalService =
        PreApprovalService(
            autoPreApprovalEnabled = autoPreApprovalEnabled,
            exemptFieldsConfig = exemptFieldsConfig,
            significanceCheckService = significanceCheckService,
            datasetJudgementSupportService = mockSupportServiceWithNoLiveDataset(),
        )

    private fun runWorkflow(
        service: PreApprovalService,
        reports: List<DataPointQaReportEntity>,
        dataPointType: String = MockDatasetJudgementEntityForTest.DUMMY_DATA_POINT_TYPE,
    ): AcceptedDataPointSource? {
        val dataPoint = buildDataPointJudgementEntity(reports, dataPointType = dataPointType)
        val entity = MockDatasetJudgementEntityForTest.createDummyDatasetJudgementEntity()
        entity.dataPoints.clear()
        entity.dataPoints.add(dataPoint)
        return service
            .preApproveDataPoints(entity)
            .dataPoints
            .first()
            .acceptedSource
    }

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
        @Test
        fun `No preapproval for exempt field even if all reports are QaAccepted`() {
            val exemptField = "exempt-field-type"
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
            val nonExemptField = "non-exempt-field-type"
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
            val exemptField = "exempt-field-type"
            val nonExemptField = "non-exempt-field-type"
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
                    exemptFieldsConfig = PreApprovalExemptFieldsConfig(mapOf(DataTypeEnum.vsme to setOf(fieldName))),
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
        ): PreApprovalService {
            val supportServiceMock = mock<DatasetJudgementSupportService>()
            whenever(supportServiceMock.getDataPointsOfLatestActiveDataset(any(), any())).thenReturn(liveDataPointMap)
            whenever(supportServiceMock.getDataPointValueNode(originalDataPointId)).thenReturn(originalValueNode)
            whenever(supportServiceMock.getDataPointValueNode(liveDataPointId)).thenReturn(liveValueNode)
            whenever(supportServiceMock.resolveBaseTypeId(dpType)).thenReturn(baseTypeId)
            return PreApprovalService(
                autoPreApprovalEnabled = true,
                exemptFieldsConfig = PreApprovalExemptFieldsConfig(),
                significanceCheckService = significanceCheckService,
                datasetJudgementSupportService = supportServiceMock,
            ).also {
                it.patchConfig(PreApprovalConfig(samplingProbability = 0.0))
            }
        }

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
