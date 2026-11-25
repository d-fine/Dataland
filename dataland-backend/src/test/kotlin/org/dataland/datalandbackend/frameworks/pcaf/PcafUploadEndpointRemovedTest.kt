package org.dataland.datalandbackend.frameworks.pcaf

import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.api.DataUploadApi
import org.dataland.datalandbackend.frameworks.sfdr.SfdrDataController
import org.dataland.datalandbackend.services.DataManager
import org.dataland.datalandbackend.utils.DefaultMocks
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

/**
 * Verifies that PCAF datasets can no longer be uploaded via the public framework data endpoint,
 * while other frameworks keep their upload endpoint.
 */
@SpringBootTest(
    classes = [DatalandBackend::class],
    properties = ["spring.profiles.active=nodb"],
)
@AutoConfigureMockMvc
@DefaultMocks
class PcafUploadEndpointRemovedTest(
    @Autowired private val mockMvc: MockMvc,
) {
    @MockitoBean
    private lateinit var dataManager: DataManager

    private val mockSecurityContext = mock<SecurityContext>()

    private val uploadRequestBody =
        """{"companyId": "${UUID.randomUUID()}", "reportingPeriod": "2024", "data": {}}"""

    @BeforeEach
    fun setup() {
        val authentication =
            AuthenticationMock.mockJwtAuthentication(
                username = "uploader",
                userId = UUID.randomUUID().toString(),
                roles = setOf(DatalandRealmRole.ROLE_USER, DatalandRealmRole.ROLE_UPLOADER, DatalandRealmRole.ROLE_ADMIN),
            )
        doReturn(authentication).whenever(mockSecurityContext).authentication
        SecurityContextHolder.setContext(mockSecurityContext)
    }

    @Test
    fun `posting a PCAF dataset returns 404 because the upload endpoint does not exist`() {
        mockMvc
            .perform(
                post("/data/pcaf")
                    .param("bypassQa", "true")
                    .contentType("application/json")
                    .content(uploadRequestBody)
                    .with(securityContext(mockSecurityContext)),
            ).andExpect(status().isNotFound)
            .andExpect(jsonPath("$.errors[0].summary").value("Dataset upload not supported."))
    }

    @Test
    fun `posting an SFDR dataset is still routed to an upload endpoint`() {
        val result =
            mockMvc
                .perform(
                    post("/data/sfdr")
                        .param("bypassQa", "true")
                        .contentType("application/json")
                        .content(uploadRequestBody)
                        .with(securityContext(mockSecurityContext)),
                ).andReturn()

        assertNotEquals(HttpStatus.NOT_FOUND.value(), result.response.status)
        assertNotEquals(HttpStatus.METHOD_NOT_ALLOWED.value(), result.response.status)
    }

    @Test
    fun `only frameworks with an upload endpoint implement the upload api`() {
        assertFalse(DataUploadApi::class.java.isAssignableFrom(PcafDataController::class.java))
        assertTrue(DataUploadApi::class.java.isAssignableFrom(SfdrDataController::class.java))
    }
}
