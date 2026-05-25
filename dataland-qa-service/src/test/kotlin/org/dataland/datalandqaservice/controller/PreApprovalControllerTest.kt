package org.dataland.datalandqaservice.controller

import org.dataland.datalandqaservice.org.dataland.datalandqaservice.controller.PreApprovalController
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalConfig
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.PreApprovalService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus

class PreApprovalControllerTest {
    private val preApprovalService: PreApprovalService = mock()
    private val controller = PreApprovalController(preApprovalService)

    @Test
    fun `getPreApprovalConfig delegates to service and returns expected body`() {
        val config = PreApprovalConfig(samplingProbability = 0.5)
        whenever(preApprovalService.config).thenReturn(config)

        val result = controller.getPreApprovalConfig()

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(config, result.body)
        verify(preApprovalService).config
    }

    @Test
    fun `patchPreApprovalConfig delegates to service and returns expected body`() {
        val config = PreApprovalConfig(samplingProbability = 0.3)
        val updatedConfig = PreApprovalConfig(samplingProbability = 0.5)
        whenever(preApprovalService.patchConfig(config)).thenReturn(updatedConfig)

        val result = controller.patchPreApprovalConfig(config)

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(updatedConfig, result.body)
        verify(preApprovalService).patchConfig(config)
    }
}
