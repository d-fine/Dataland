package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.utils.DefaultMocks
import org.dataland.datalandproxyfinder.openApiClient.api.ProxySuggestionControllerApi
import org.dataland.datalandproxyfinder.openApiClient.infrastructure.ClientError
import org.dataland.datalandproxyfinder.openApiClient.infrastructure.ClientException
import org.dataland.datalandproxyfinder.openApiClient.model.ProxySuggestion
import org.dataland.datalandproxyfinder.openApiClient.model.ProxySuggestionSource
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@SpringBootTest(
    classes = [DatalandBackend::class],
    properties = ["spring.profiles.active=nodb"],
)
@AutoConfigureMockMvc
@DefaultMocks
class CompanyProxySuggestionControllerTest(
    @Autowired private val mockMvc: MockMvc,
) {
    companion object {
        private const val PROXY_SUGGESTION_ENDPOINT = "/company-proxies/{companyId}/proxy-suggestion"
    }

    @MockitoBean
    private lateinit var proxySuggestionControllerApi: ProxySuggestionControllerApi

    private val mockSecurityContext = mock<SecurityContext>()

    @BeforeEach
    fun setup() {
        reset(mockSecurityContext, proxySuggestionControllerApi)
    }

    private fun requestProxySuggestion(
        companyId: String,
        role: DatalandRealmRole = DatalandRealmRole.ROLE_ADMIN,
    ): ResultActions {
        val authentication =
            AuthenticationMock.mockJwtAuthentication(
                username = role.name,
                userId = UUID.randomUUID().toString(),
                roles = setOf(role, DatalandRealmRole.ROLE_USER),
            )
        doReturn(authentication).whenever(mockSecurityContext).authentication
        return mockMvc.perform(post(PROXY_SUGGESTION_ENDPOINT, companyId).with(securityContext(mockSecurityContext)))
    }

    private fun proxyFinderClientException(
        statusCode: Int,
        body: String? = null,
    ) = ClientException(
        message = "Client error",
        statusCode = statusCode,
        response = ClientError<Any>(body = body, statusCode = statusCode),
    )

    @Test
    fun `an admin receives the proxy suggestion of the proxy finder`() {
        val companyId = UUID.randomUUID().toString()
        val proxyCompanyId = UUID.randomUUID().toString()
        doReturn(
            ProxySuggestion(
                companyId = companyId,
                proxyCompanyId = proxyCompanyId,
                confidence = 0.5,
                reasoning = "Mocked reasoning",
                sources = listOf(ProxySuggestionSource(title = "Source", url = "https://example.com")),
            ),
        ).whenever(proxySuggestionControllerApi).findProxySuggestion(companyId)

        requestProxySuggestion(companyId)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.companyId").value(companyId))
            .andExpect(jsonPath("$.proxyCompanyId").value(proxyCompanyId))
            .andExpect(jsonPath("$.confidence").value(0.5))
            .andExpect(jsonPath("$.reasoning").value("Mocked reasoning"))
            .andExpect(jsonPath("$.sources[0].title").value("Source"))
            .andExpect(jsonPath("$.sources[0].url").value("https://example.com"))
    }

    @Test
    fun `non admin users get a 403 when requesting a proxy suggestion and the proxy finder is not called`() {
        requestProxySuggestion(UUID.randomUUID().toString(), DatalandRealmRole.ROLE_UPLOADER)
            .andExpect(status().isForbidden)
        verify(proxySuggestionControllerApi, never()).findProxySuggestion(any())
    }

    @Test
    fun `an unknown company reported by the proxy finder results in a 404`() {
        val companyId = UUID.randomUUID().toString()
        doThrow(proxyFinderClientException(404)).whenever(proxySuggestionControllerApi).findProxySuggestion(companyId)
        requestProxySuggestion(companyId).andExpect(status().isNotFound)
    }

    @Test
    fun `an invalid suggestion reported by the proxy finder is forwarded as a 422`() {
        val companyId = UUID.randomUUID().toString()
        val errorBody =
            """{"errors":[{"errorType":"invalid-proxy-suggestion","summary":"Suggested proxy company is the company itself.",""" +
                """"message":"self reference","httpStatus":422}]}"""
        doThrow(proxyFinderClientException(422, errorBody))
            .whenever(proxySuggestionControllerApi)
            .findProxySuggestion(companyId)
        requestProxySuggestion(companyId)
            .andExpect(status().isUnprocessableContent)
            .andExpect(jsonPath("$.errors[0].errorType").value("invalid-proxy-suggestion"))
            .andExpect(jsonPath("$.errors[0].summary").value("Suggested proxy company is the company itself."))
            .andExpect(jsonPath("$.errors[0].message").value("self reference"))
    }
}
