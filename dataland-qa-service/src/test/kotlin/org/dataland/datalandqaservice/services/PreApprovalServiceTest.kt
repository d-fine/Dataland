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
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalCheckResults
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalConfig
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DatasetJudgementSupportService
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.PreApprovalService
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.SignificanceCheckService
import org.dataland.datalandqaservice.utils.MockDatasetJudgementEntityForTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
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

    @Suppress("LongParameterList")
    private fun buildServiceWithLiveDatasetForSignificanceCheck(
        originalDataPointId: String,
        liveDataPointId: String,
        originalValueNode: JsonNode?,
        liveValueNode: JsonNode?,
        baseTypeId: String,
        dpType: String,
        liveDataPointMap: Map<String, String> = mapOf(dpType to liveDataPointId),
        significanceCheckService: SignificanceCheckService = this.significanceCheckService,
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

    @Nested
    inner class PreApprovalCheckResultsTest {
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
            assertEquals(false, checkResults.isDataPointEligible)

            // Scenario 2: data point type is not on the exempt field list
            checkResults = requireNotNull(runWorkflowAndReturnCheckResults(service, emptyList(), dataPointType = nonExemptField))
            assertEquals(true, checkResults.isDataPointEligible)
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
}
