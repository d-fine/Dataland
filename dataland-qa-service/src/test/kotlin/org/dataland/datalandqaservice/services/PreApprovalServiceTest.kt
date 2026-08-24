package org.dataland.datalandqaservice.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.DecimalNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.TextNode
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.exceptions.InternalServerErrorApiException
import org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource
import org.dataland.datalandqaservice.model.reports.QaReportDataPointVerdict
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaConfigEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalConfig
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalConfigPatchRequest
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalConfigPutRequest
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.QaConfigRepository
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.PreApprovalService
import org.dataland.datalandqaservice.utils.MockDatasetJudgementEntityForTest
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils.DUMMY_SUBMIT_USER_ID
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
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.util.Optional
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
            val service =
                buildServiceWithoutLiveDataset(
                    autoPreApprovalEnabled = true,
                    exemptFields = mapOf(DataTypeEnum.lksg to setOf(fieldName)),
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

    @Nested
    inner class ConfigLoadingTests {
        @Test
        fun `config is loaded from the database on startup`() {
            val seededConfig =
                PreApprovalConfig(
                    exemptFields = mapOf(DataTypeEnum.sfdr to setOf("some-field")),
                    samplingProbability = 0.33,
                    decimalRelativeThreshold = 0.25,
                    integerAbsoluteThreshold = 3,
                    autoPreApprovalEnabled = false,
                    submitUserId = "some-previous-submitter",
                )
            val repository = PreApprovalServiceTestUtils.buildQaConfigRepositoryMock(seededConfig)
            val service =
                PreApprovalService(
                    qaConfigRepository = repository,
                    significanceCheckService = PreApprovalServiceTestUtils.significanceCheckService,
                    datasetJudgementSupportService = PreApprovalServiceTestUtils.mockSupportServiceWithNoLiveDataset(),
                )

            service.initializeConfig()

            assertEquals(seededConfig, service.config)
        }

        @Test
        fun `config remains unavailable and startup does not throw when no row exists in the database`() {
            val repository = mock<QaConfigRepository>()
            whenever(repository.findById(QaConfigEntity.QA_CONFIG_SINGLETON_ID)).thenReturn(Optional.empty())
            val service =
                PreApprovalService(
                    qaConfigRepository = repository,
                    significanceCheckService = PreApprovalServiceTestUtils.significanceCheckService,
                    datasetJudgementSupportService = PreApprovalServiceTestUtils.mockSupportServiceWithNoLiveDataset(),
                )

            service.initializeConfig()

            // However, accessing the PreApprovalConfig should throw an error.
            assertThrows<InternalServerErrorApiException> { service.config }
        }

        @Test
        fun `preApproveDataPoints is a no-op and does not throw when the config is unavailable`() {
            val repository = mock<QaConfigRepository>()
            whenever(repository.findById(QaConfigEntity.QA_CONFIG_SINGLETON_ID)).thenReturn(Optional.empty())
            val service =
                PreApprovalService(
                    qaConfigRepository = repository,
                    significanceCheckService = PreApprovalServiceTestUtils.significanceCheckService,
                    datasetJudgementSupportService = PreApprovalServiceTestUtils.mockSupportServiceWithNoLiveDataset(),
                )
            service.initializeConfig()

            val dataPoint =
                buildDataPointJudgementEntity(
                    qaReports =
                        listOf(
                            buildQaReport(
                                reporterUserId = dummyReporter1,
                                verdict = QaReportDataPointVerdict.QaAccepted,
                            ),
                        ),
                )
            val datasetJudgementEntity =
                MockDatasetJudgementEntityForTest.createDummyDatasetJudgementEntity().also { entity ->
                    entity.dataPoints.clear()
                    entity.dataPoints.add(dataPoint)
                }

            val result = service.preApproveDataPoints(datasetJudgementEntity)

            assertEquals(datasetJudgementEntity, result)
            assertEquals(null, dataPoint.acceptedSource)
        }
    }

    @Nested
    inner class PatchConfigTests {
        @Test
        fun `patchConfig merges only the provided fields and leaves others untouched`() {
            val service = buildServiceWithoutLiveDataset(autoPreApprovalEnabled = true)
            val before = service.config

            val updated =
                service.patchConfig(
                    PreApprovalConfigPatchRequest(samplingProbability = 0.55),
                    DUMMY_SUBMIT_USER_ID,
                )

            assertEquals(0.55, updated.samplingProbability)
            assertEquals(before.exemptFields, updated.exemptFields)
            assertEquals(before.decimalRelativeThreshold, updated.decimalRelativeThreshold)
            assertEquals(before.integerAbsoluteThreshold, updated.integerAbsoluteThreshold)
            assertEquals(before.individualDecimalThresholds, updated.individualDecimalThresholds)
            assertEquals(before.individualIntegerThresholds, updated.individualIntegerThresholds)
            assertEquals(before.autoPreApprovalEnabled, updated.autoPreApprovalEnabled)
        }

        @Test
        fun `patchConfig sets submitUserId server-side`() {
            val service = buildServiceWithoutLiveDataset(autoPreApprovalEnabled = true)

            val updated =
                service.patchConfig(PreApprovalConfigPatchRequest(samplingProbability = 0.1), DUMMY_SUBMIT_USER_ID)

            assertEquals(DUMMY_SUBMIT_USER_ID, updated.submitUserId)
        }

        @Test
        fun `patchConfig persists the merged config to the database`() {
            val repository =
                PreApprovalServiceTestUtils.buildQaConfigRepositoryMock(
                    PreApprovalConfig(autoPreApprovalEnabled = true),
                )
            val service =
                PreApprovalService(
                    qaConfigRepository = repository,
                    significanceCheckService = PreApprovalServiceTestUtils.significanceCheckService,
                    datasetJudgementSupportService = PreApprovalServiceTestUtils.mockSupportServiceWithNoLiveDataset(),
                ).also { it.initializeConfig() }

            service.patchConfig(PreApprovalConfigPatchRequest(samplingProbability = 0.9), DUMMY_SUBMIT_USER_ID)

            argumentCaptor<QaConfigEntity>().apply {
                verify(repository).save(capture())
                assertEquals(0.9, firstValue.config.samplingProbability)
            }
        }

        @Test
        fun `patchConfig is a no-op when the resulting config is unchanged`() {
            val repository =
                PreApprovalServiceTestUtils.buildQaConfigRepositoryMock(
                    PreApprovalConfig(samplingProbability = 0.5, autoPreApprovalEnabled = true),
                )
            val service =
                PreApprovalService(
                    qaConfigRepository = repository,
                    significanceCheckService = PreApprovalServiceTestUtils.significanceCheckService,
                    datasetJudgementSupportService = PreApprovalServiceTestUtils.mockSupportServiceWithNoLiveDataset(),
                ).also { it.initializeConfig() }

            service.patchConfig(PreApprovalConfigPatchRequest(samplingProbability = 0.5), DUMMY_SUBMIT_USER_ID)

            verify(repository, never()).save(any())
        }
    }

    @Nested
    inner class PutConfigTests {
        @Test
        fun `putConfig fully replaces the config`() {
            val service =
                buildServiceWithoutLiveDataset(
                    autoPreApprovalEnabled = true,
                    exemptFields = mapOf(DataTypeEnum.sfdr to setOf("some-field")),
                )

            val replacement =
                PreApprovalConfigPutRequest(
                    exemptFields = emptyMap(),
                    samplingProbability = 0.8,
                    decimalRelativeThreshold = 0.9,
                    integerAbsoluteThreshold = 7,
                    individualDecimalThresholds = emptyMap(),
                    individualIntegerThresholds = emptyMap(),
                    autoPreApprovalEnabled = false,
                )

            val updated = service.putConfig(replacement, DUMMY_SUBMIT_USER_ID)

            assertEquals(emptyMap<DataTypeEnum, Set<String>>(), updated.exemptFields)
            assertEquals(0.8, updated.samplingProbability)
            assertEquals(0.9, updated.decimalRelativeThreshold)
            assertEquals(7L, updated.integerAbsoluteThreshold)
            assertEquals(false, updated.autoPreApprovalEnabled)
            assertEquals(DUMMY_SUBMIT_USER_ID, updated.submitUserId)
        }

        @Test
        fun `putConfig persists the replaced config to the database`() {
            val repository =
                PreApprovalServiceTestUtils.buildQaConfigRepositoryMock(
                    PreApprovalConfig(autoPreApprovalEnabled = true),
                )
            val service =
                PreApprovalService(
                    qaConfigRepository = repository,
                    significanceCheckService = PreApprovalServiceTestUtils.significanceCheckService,
                    datasetJudgementSupportService = PreApprovalServiceTestUtils.mockSupportServiceWithNoLiveDataset(),
                ).also { it.initializeConfig() }

            val replacement =
                PreApprovalConfigPutRequest(
                    exemptFields = emptyMap(),
                    samplingProbability = 0.3,
                    decimalRelativeThreshold = 0.5,
                    integerAbsoluteThreshold = 5,
                    individualDecimalThresholds = emptyMap(),
                    individualIntegerThresholds = emptyMap(),
                    autoPreApprovalEnabled = true,
                )
            service.putConfig(replacement, DUMMY_SUBMIT_USER_ID)

            argumentCaptor<QaConfigEntity>().apply {
                verify(repository).save(capture())
                assertEquals(0.3, firstValue.config.samplingProbability)
                assertEquals(0.5, firstValue.config.decimalRelativeThreshold)
                assertEquals(5, firstValue.config.integerAbsoluteThreshold)
                assert(firstValue.config.individualDecimalThresholds.isEmpty())
                assert(firstValue.config.individualIntegerThresholds.isEmpty())
                assert(firstValue.config.autoPreApprovalEnabled)
            }
        }

        @Test
        fun `putConfig is a no-op when the resulting config is unchanged`() {
            val initialConfig =
                PreApprovalConfig(
                    exemptFields = emptyMap(),
                    samplingProbability = 0.5,
                    decimalRelativeThreshold = 0.5,
                    integerAbsoluteThreshold = 5,
                    individualDecimalThresholds = emptyMap(),
                    individualIntegerThresholds = emptyMap(),
                    autoPreApprovalEnabled = true,
                )
            val repository = PreApprovalServiceTestUtils.buildQaConfigRepositoryMock(initialConfig)
            val service =
                PreApprovalService(
                    qaConfigRepository = repository,
                    significanceCheckService = PreApprovalServiceTestUtils.significanceCheckService,
                    datasetJudgementSupportService = PreApprovalServiceTestUtils.mockSupportServiceWithNoLiveDataset(),
                ).also { it.initializeConfig() }

            val identicalReplacement =
                PreApprovalConfigPutRequest(
                    exemptFields = initialConfig.exemptFields,
                    samplingProbability = initialConfig.samplingProbability,
                    decimalRelativeThreshold = initialConfig.decimalRelativeThreshold,
                    integerAbsoluteThreshold = initialConfig.integerAbsoluteThreshold,
                    individualDecimalThresholds = initialConfig.individualDecimalThresholds,
                    individualIntegerThresholds = initialConfig.individualIntegerThresholds,
                    autoPreApprovalEnabled = initialConfig.autoPreApprovalEnabled,
                )
            service.putConfig(identicalReplacement, DUMMY_SUBMIT_USER_ID)

            verify(repository, never()).save(any())
        }
    }

    @Nested
    inner class IndividualThresholdConfigTests {
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
