package org.dataland.datalandqaservice.controller

import org.dataland.datalandqaservice.org.dataland.datalandqaservice.controller.PreApprovalController
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalConfig
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalConfigPatchRequest
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalConfigPutRequest
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.PreApprovalService
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import java.util.UUID

class PreApprovalControllerTest {
    private val preApprovalService: PreApprovalService = mock()
    private val controller = PreApprovalController(preApprovalService)
    private val dummySubmitUserId = UUID.randomUUID().toString()

    @BeforeEach
    fun setup() {
        AuthenticationMock.mockSecurityContext(
            "data.admin@example.com",
            dummySubmitUserId,
            setOf(DatalandRealmRole.ROLE_ADMIN),
        )
    }

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
    fun `patchPreApprovalConfig delegates to service with the authenticated user id and returns expected body`() {
        val patch = PreApprovalConfigPatchRequest(samplingProbability = 0.5)
        val updatedConfig = PreApprovalConfig(samplingProbability = 0.5, submitUserId = dummySubmitUserId)
        whenever(preApprovalService.patchConfig(eq(patch), any())).thenReturn(updatedConfig)

        val result = controller.patchPreApprovalConfig(patch)

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(updatedConfig, result.body)
        verify(preApprovalService).patchConfig(patch, dummySubmitUserId)
    }

    @Test
    fun `putPreApprovalConfig delegates to service with the authenticated user id and returns expected body`() {
        val newConfig =
            PreApprovalConfigPutRequest(
                exemptFields = emptyMap(),
                samplingProbability = 0.5,
                decimalRelativeThreshold = 0.5,
                integerAbsoluteThreshold = 5L,
                individualDecimalThresholds = emptyMap(),
                individualIntegerThresholds = emptyMap(),
                autoPreApprovalEnabled = true,
            )
        val replacedConfig = PreApprovalConfig(samplingProbability = 0.5, submitUserId = dummySubmitUserId)
        whenever(preApprovalService.putConfig(eq(newConfig), any())).thenReturn(replacedConfig)

        val result = controller.putPreApprovalConfig(newConfig)

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(replacedConfig, result.body)
        verify(preApprovalService).putConfig(newConfig, dummySubmitUserId)
    }
}
