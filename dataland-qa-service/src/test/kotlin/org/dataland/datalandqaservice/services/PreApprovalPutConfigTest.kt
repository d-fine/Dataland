package org.dataland.datalandqaservice.services

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaConfigEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalConfig
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalConfigPutRequest
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.PreApprovalService
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils.DUMMY_SUBMIT_USER_ID
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils.buildServiceWithoutLiveDataset
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

/**
 * Tests that [PreApprovalService.putConfig] fully replaces the config, persists the result, and is a no-op
 * when nothing changes.
 */
class PreApprovalPutConfigTest {
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
