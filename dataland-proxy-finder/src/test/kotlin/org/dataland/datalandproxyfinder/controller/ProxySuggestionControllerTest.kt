package org.dataland.datalandproxyfinder.controller

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.CompanyId
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.IdentifierType
import org.dataland.datalandbackendutils.exceptions.INVALID_PROXY_SUGGESTION_ERROR_TYPE
import org.dataland.datalandproxyfinder.DatalandProxyFinder
import org.dataland.datalandproxyfinder.services.MockProxySuggestionProvider
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
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
    classes = [DatalandProxyFinder::class],
    properties = ["spring.profiles.active=nodb", "dataland.proxy-finder.mock.proxy-company-lei=$PROXY_COMPANY_LEI"],
)
@AutoConfigureMockMvc
class ProxySuggestionControllerTest(
    @Autowired private val mockMvc: MockMvc,
) {
    @MockitoBean
    private lateinit var companyDataControllerApi: CompanyDataControllerApi

    private val companyId = UUID.randomUUID().toString()
    private val proxyCompanyId = UUID.randomUUID().toString()

    @BeforeEach
    fun setup() {
        reset(companyDataControllerApi)
        doReturn(CompanyInformation("Some Company", "Berlin", emptyMap(), "DE"))
            .whenever(companyDataControllerApi)
            .getCompanyInfo(companyId)
        doReturn(CompanyId(proxyCompanyId))
            .whenever(companyDataControllerApi)
            .getCompanyIdByIdentifier(IdentifierType.Lei, PROXY_COMPANY_LEI)
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
        val mockSecurityContext = mock<SecurityContext>()
        doReturn(authentication).whenever(mockSecurityContext).authentication
        return mockMvc.perform(post("/proxy-suggestions/{companyId}", companyId).with(securityContext(mockSecurityContext)))
    }

    @Test
    fun `an admin receives the hardcoded proxy suggestion`() {
        requestProxySuggestion(companyId)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.companyId").value(companyId))
            .andExpect(jsonPath("$.proxyCompanyId").value(proxyCompanyId))
            .andExpect(jsonPath("$.confidence").value(MockProxySuggestionProvider.MOCK_CONFIDENCE))
            .andExpect(jsonPath("$.reasoning").value(MockProxySuggestionProvider.MOCK_REASONING))
            .andExpect(jsonPath("$.sources.length()").value(MockProxySuggestionProvider.MOCK_SOURCES.size))
    }

    @Test
    fun `non admin users are forbidden to request proxy suggestions`() {
        listOf(DatalandRealmRole.ROLE_USER, DatalandRealmRole.ROLE_UPLOADER, DatalandRealmRole.ROLE_REVIEWER).forEach {
            requestProxySuggestion(companyId, it).andExpect(status().isForbidden)
        }
    }

    @Test
    fun `requesting a suggestion for an unknown company returns 404`() {
        val unknownCompanyId = UUID.randomUUID().toString()
        doThrow(ClientException(statusCode = 404)).whenever(companyDataControllerApi).getCompanyInfo(unknownCompanyId)
        requestProxySuggestion(unknownCompanyId).andExpect(status().isNotFound)
    }

    @Test
    fun `a suggestion of the company itself returns 422`() {
        doReturn(CompanyId(companyId))
            .whenever(companyDataControllerApi)
            .getCompanyIdByIdentifier(IdentifierType.Lei, PROXY_COMPANY_LEI)
        requestProxySuggestion(companyId)
            .andExpect(status().isUnprocessableContent)
            .andExpect(jsonPath("$.errors[0].errorType").value(INVALID_PROXY_SUGGESTION_ERROR_TYPE))
    }

    @Test
    fun `a suggestion of an unknown company returns 422`() {
        doThrow(ClientException(statusCode = 404)).whenever(companyDataControllerApi).isCompanyIdValid(any())
        requestProxySuggestion(companyId)
            .andExpect(status().isUnprocessableContent)
            .andExpect(jsonPath("$.errors[0].errorType").value(INVALID_PROXY_SUGGESTION_ERROR_TYPE))
    }
}

private const val PROXY_COMPANY_LEI = "TESTPROXYCOMPANYLEI1"
