package org.dataland.datalandqaservice.services

import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaConfigEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalConfig
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalConfigPatchRequest
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
 * Tests that [PreApprovalService.patchConfig] merges the provided fields correctly, persists the result, and
 * is a no-op when nothing changes.
 */
class PreApprovalPatchConfigTest {
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
