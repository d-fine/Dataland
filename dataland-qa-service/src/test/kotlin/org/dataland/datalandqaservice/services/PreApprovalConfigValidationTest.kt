package org.dataland.datalandqaservice.services

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaConfigEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalConfig
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalConfigPatchRequest
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalConfigPutRequest
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.QaConfigRepository
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.PreApprovalService
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils.DUMMY_SUBMIT_USER_ID
import org.dataland.datalandspecificationservice.openApiClient.api.SpecificationControllerApi
import org.dataland.datalandspecificationservice.openApiClient.model.FrameworkSpecification
import org.dataland.datalandspecificationservice.openApiClient.model.IdWithRef
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Optional

/**
 * Tests that [PreApprovalService.putConfig] (and, by extension, [PreApprovalService.patchConfig], which shares
 * the same validation logic) rejects invalid pre-approval configurations: overlaps between exempt fields and
 * individual thresholds, overlaps between individual decimal and integer thresholds, unknown data point type
 * IDs, and duplicate data point type IDs within a single exemptFields list.
 */
class PreApprovalConfigValidationTest {
    private val knownDataPointType = "known-field"

    private val frameworkSpecification =
        FrameworkSpecification(
            framework = IdWithRef(id = "sfdrID", ref = "sfdrRef"),
            name = "SFDR",
            businessDefinition = "Test framework specification for validation tests.",
            schema =
                """
                {
                  "general": {
                    "key1": {
                      "id": "$knownDataPointType",
                      "ref": "ref1"
                    }
                  }
                }
                """.trimIndent(),
            referencedReportJsonPath = "/reports/testReport.json",
        )

    private fun buildServiceWithFrameworkSpecification(): PreApprovalService {
        val specificationServiceMock = mock<SpecificationControllerApi>()
        whenever(specificationServiceMock.getFrameworkSpecification(any())).thenReturn(frameworkSpecification)
        val repository = mock<QaConfigRepository>()
        whenever(repository.findById(QaConfigEntity.QA_CONFIG_SINGLETON_ID)).thenReturn(
            Optional.of(QaConfigEntity(config = PreApprovalConfig())),
        )
        return PreApprovalService(
            qaConfigRepository = repository,
            significanceCheckService = PreApprovalServiceTestUtils.significanceCheckService,
            datasetJudgementSupportService = PreApprovalServiceTestUtils.mockSupportServiceWithNoLiveDataset(),
            specificationService = specificationServiceMock,
        ).also { it.initializeConfig() }
    }

    private fun baseReplacement(
        exemptFields: Map<DataTypeEnum, List<String>> = emptyMap(),
        individualDecimalThresholds: Map<DataTypeEnum, Map<String, Double>> = emptyMap(),
        individualIntegerThresholds: Map<DataTypeEnum, Map<String, Long>> = emptyMap(),
    ) = PreApprovalConfigPutRequest(
        exemptFields = exemptFields,
        samplingProbability = 0.0,
        decimalRelativeThreshold = 0.5,
        integerAbsoluteThreshold = 5,
        individualDecimalThresholds = individualDecimalThresholds,
        individualIntegerThresholds = individualIntegerThresholds,
        autoPreApprovalEnabled = true,
    )

    @Test
    fun `putConfig throws when a data point type is both exempt and individually decimal-thresholded`() {
        val service = buildServiceWithFrameworkSpecification()

        val exception =
            assertThrows<InvalidInputApiException> {
                service.putConfig(
                    baseReplacement(
                        exemptFields = mapOf(DataTypeEnum.sfdr to listOf(knownDataPointType)),
                        individualDecimalThresholds = mapOf(DataTypeEnum.sfdr to mapOf(knownDataPointType to 0.1)),
                    ),
                    DUMMY_SUBMIT_USER_ID,
                )
            }

        assertTrue(exception.message?.contains("exempt and individually thresholded") == true)
    }

    @Test
    fun `putConfig throws when a data point type is both exempt and individually integer-thresholded`() {
        val service = buildServiceWithFrameworkSpecification()

        val exception =
            assertThrows<InvalidInputApiException> {
                service.putConfig(
                    baseReplacement(
                        exemptFields = mapOf(DataTypeEnum.sfdr to listOf(knownDataPointType)),
                        individualIntegerThresholds = mapOf(DataTypeEnum.sfdr to mapOf(knownDataPointType to 1L)),
                    ),
                    DUMMY_SUBMIT_USER_ID,
                )
            }

        assertTrue(exception.message?.contains("exempt and individually thresholded") == true)
    }

    @Test
    fun `putConfig throws when a data point type has both an individual decimal and integer threshold`() {
        val service = buildServiceWithFrameworkSpecification()

        val exception =
            assertThrows<InvalidInputApiException> {
                service.putConfig(
                    baseReplacement(
                        individualDecimalThresholds = mapOf(DataTypeEnum.sfdr to mapOf(knownDataPointType to 0.1)),
                        individualIntegerThresholds = mapOf(DataTypeEnum.sfdr to mapOf(knownDataPointType to 1L)),
                    ),
                    DUMMY_SUBMIT_USER_ID,
                )
            }

        assertTrue(exception.message?.contains("both an individual decimal and integer threshold") == true)
    }

    @Test
    fun `putConfig throws when an unknown data point type ID is configured`() {
        val service = buildServiceWithFrameworkSpecification()

        val exception =
            assertThrows<InvalidInputApiException> {
                service.putConfig(
                    baseReplacement(exemptFields = mapOf(DataTypeEnum.sfdr to listOf("unknown-field"))),
                    DUMMY_SUBMIT_USER_ID,
                )
            }

        assertTrue(exception.message?.contains("Unknown data point type IDs") == true)
    }

    @Test
    fun `putConfig throws when exemptFields contains a duplicate data point type ID for a framework`() {
        val service = buildServiceWithFrameworkSpecification()

        val exception =
            assertThrows<InvalidInputApiException> {
                service.putConfig(
                    baseReplacement(
                        exemptFields = mapOf(DataTypeEnum.sfdr to listOf(knownDataPointType, knownDataPointType)),
                    ),
                    DUMMY_SUBMIT_USER_ID,
                )
            }

        assertTrue(exception.message?.contains("Duplicate data point type IDs") == true)
    }

    @Test
    fun `patchConfig throws when exemptFields contains a duplicate data point type ID for a framework`() {
        val service = buildServiceWithFrameworkSpecification()

        val exception =
            assertThrows<InvalidInputApiException> {
                service.patchConfig(
                    PreApprovalConfigPatchRequest(
                        exemptFields = mapOf(DataTypeEnum.sfdr to listOf(knownDataPointType, knownDataPointType)),
                    ),
                    DUMMY_SUBMIT_USER_ID,
                )
            }

        assertTrue(exception.message?.contains("Duplicate data point type IDs") == true)
    }

    @Test
    fun `putConfig succeeds when the same data point type is configured only once and only in one category`() {
        val service = buildServiceWithFrameworkSpecification()

        val updated =
            service.putConfig(
                baseReplacement(exemptFields = mapOf(DataTypeEnum.sfdr to listOf(knownDataPointType))),
                DUMMY_SUBMIT_USER_ID,
            )

        assertTrue(updated.exemptFields[DataTypeEnum.sfdr]?.contains(knownDataPointType) == true)
    }
}
