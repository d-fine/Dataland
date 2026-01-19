package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.model.proxies.CompanyProxy
import org.dataland.datalandbackend.utils.DefaultMocks
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@SpringBootTest(
    classes = [DatalandBackend::class],
    properties = ["spring.profiles.active=nodb"],
)
@AutoConfigureMockMvc
@DefaultMocks
class CompanyProxyControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
) {
    companion object {
        private const val COMPANY_PROXIES_ENDPOINT = "/company-proxies"
        private const val COMPANY_PROXIES_BY_ID_ENDPOINT = "/company-proxies/{proxyId}"
        private const val COMPANIES_ENDPOINT = "/companies"
        private const val APPLICATION_JSON = "application/json"
        private const val COMPANY_ID_JSON_FIELD = "companyId"
        private const val JSON_PATH_PROXIED_COMPANY_ID = "\$.proxiedCompanyId"
        private const val JSON_PATH_PROXY_COMPANY_ID = "\$.proxyCompanyId"
        private const val JSON_PATH_FRAMEWORK = "\$.framework"
        private const val JSON_PATH_REPORTING_PERIOD = "\$.reportingPeriod"
    }

    @MockitoBean
    @Qualifier("AuthenticatedOkHttpClient")
    private lateinit var authenticatedOkHttpClient: OkHttpClient

    private val mockSecurityContext = mock<SecurityContext>()

    private val adminUserId = UUID.randomUUID()
    private val uploaderUserId = UUID.randomUUID()

    private val adminAuthentication: DatalandJwtAuthentication =
        AuthenticationMock.mockJwtAuthentication(
            username = "ADMIN",
            userId = adminUserId.toString(),
            roles = setOf(DatalandRealmRole.ROLE_ADMIN, DatalandRealmRole.ROLE_USER),
        )

    private val uploaderAuthentication: DatalandJwtAuthentication =
        AuthenticationMock.mockJwtAuthentication(
            username = "UPLOADER",
            userId = uploaderUserId.toString(),
            roles = setOf(DatalandRealmRole.ROLE_UPLOADER, DatalandRealmRole.ROLE_USER),
        )

    enum class User { ADMIN, UPLOADER }

    @BeforeEach
    fun setup() {
        reset(mockSecurityContext, authenticatedOkHttpClient)
        setupCompanyExistsValidatorMocks()
    }

    private fun setMockUser(user: User) {
        setMockSecurityContext(
            when (user) {
                User.ADMIN -> adminAuthentication
                User.UPLOADER -> uploaderAuthentication
            },
        )
    }

    private fun setMockSecurityContext(authentication: DatalandJwtAuthentication) {
        doReturn(authentication).whenever(mockSecurityContext).authentication
        SecurityContextHolder.setContext(mockSecurityContext)
    }

    /**
     * Stub the HTTP call used by CompanyExistsValidator so it always returns 200 OK.
     * This prevents any real HTTP / Keycloak calls during tests.
     */
    private fun setupCompanyExistsValidatorMocks() {
        val mockCall = mock<okhttp3.Call>()
        val dummyBody =
            object : ResponseBody() {
                override fun contentType(): okhttp3.MediaType? = null

                override fun contentLength(): Long = 0

                override fun source(): BufferedSource = Buffer()
            }
        val response =
            Response
                .Builder()
                .request(
                    Request
                        .Builder()
                        .url("http://localhost/dummy")
                        .build(),
                ).protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(dummyBody)
                .build()
        whenever(authenticatedOkHttpClient.newCall(any())).thenReturn(mockCall)
        whenever(mockCall.execute()).thenReturn(response)
    }

    /**
     * Create a company via POST /companies and return its companyId.
     * Uses a unique LEI every time to avoid DuplicateIdentifierApiException.
     */
    private fun createCompany(): String {
        setMockUser(User.ADMIN)
        val randomLei =
            UUID
                .randomUUID()
                .toString()
                .replace("-", "")
                .take(20)
        val companyRequest =
            mapOf(
                "companyName" to "ABC Corporation $randomLei",
                "headquarters" to "Berlin",
                "identifiers" to
                    mapOf(
                        "Lei" to listOf(randomLei),
                    ),
                "countryCode" to "DE",
            )
        val companyRequestJson = objectMapper.writeValueAsString(companyRequest)
        val result =
            mockMvc
                .perform(
                    post(COMPANIES_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .content(companyRequestJson)
                        .with(securityContext(mockSecurityContext)),
                ).andExpect(status().isOk)
                .andReturn()
        return objectMapper
            .readTree(result.response.contentAsString)
            .get(COMPANY_ID_JSON_FIELD)
            .asText()
    }

    private fun performProxyRequest(
        method: String,
        endpoint: String,
        pathVar: String? = null,
        requestBody: CompanyProxy<String>,
        user: User = User.ADMIN,
    ): ResultActions {
        setMockUser(user)
        val req = objectMapper.writeValueAsString(requestBody)
        val builder =
            when (method) {
                "POST" -> post(endpoint)
                "PUT" -> if (pathVar != null) put(endpoint, pathVar) else put(endpoint)
                else -> throw IllegalArgumentException("Unsupported method $method")
            }
        return mockMvc.perform(
            builder
                .contentType(APPLICATION_JSON)
                .content(req)
                .with(securityContext(mockSecurityContext)),
        )
    }

    private fun getProxyIdFromResult(result: ResultActions): String =
        objectMapper.readTree(result.andReturn().response.contentAsString)["proxyId"].asText()

    @Test
    fun `create proxy then get by id returns correct data`() {
        val companyIdProxyCompany = createCompany()
        val companyIdProxiedCompany = createCompany()
        val requestBody = CompanyProxy(companyIdProxiedCompany, companyIdProxyCompany, "sfdr", "2024")

        val postResult =
            performProxyRequest("POST", COMPANY_PROXIES_ENDPOINT, null, requestBody)
                .andExpect(status().isOk)
                .andExpect(jsonPath(JSON_PATH_PROXIED_COMPANY_ID).value(companyIdProxiedCompany))
                .andExpect(jsonPath(JSON_PATH_PROXY_COMPANY_ID).value(companyIdProxyCompany))
                .andExpect(jsonPath(JSON_PATH_FRAMEWORK).value("sfdr"))
                .andExpect(jsonPath(JSON_PATH_REPORTING_PERIOD).value("2024"))

        val proxyId = getProxyIdFromResult(postResult)
        mockMvc
            .perform(
                get(COMPANY_PROXIES_BY_ID_ENDPOINT, proxyId)
                    .contentType(APPLICATION_JSON)
                    .with(securityContext(mockSecurityContext)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.proxyId").value(proxyId))
            .andExpect(jsonPath(JSON_PATH_PROXIED_COMPANY_ID).value(companyIdProxiedCompany))
            .andExpect(jsonPath(JSON_PATH_PROXY_COMPANY_ID).value(companyIdProxyCompany))
            .andExpect(jsonPath(JSON_PATH_FRAMEWORK).value("sfdr"))
            .andExpect(jsonPath(JSON_PATH_REPORTING_PERIOD).value("2024"))
    }

    @Test
    fun `creating a proxy with invalid id returns an error`() {
        val invalidRequest = CompanyProxy("123", "456", "lksg", "2023")
        performProxyRequest("POST", COMPANY_PROXIES_ENDPOINT, null, invalidRequest)
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `create proxy then delete proxy and assert that it is no longer retrievable`() {
        val companyIdProxyCompany = createCompany()
        val companyIdProxiedCompany = createCompany()
        val requestBody = CompanyProxy(companyIdProxiedCompany, companyIdProxyCompany, "sfdr", "2024")
        val postResult =
            performProxyRequest("POST", COMPANY_PROXIES_ENDPOINT, null, requestBody)
                .andExpect(status().isOk)
        val proxyId = getProxyIdFromResult(postResult)
        setMockUser(User.ADMIN)
        mockMvc
            .perform(
                delete(COMPANY_PROXIES_BY_ID_ENDPOINT, proxyId)
                    .contentType(APPLICATION_JSON)
                    .with(securityContext(mockSecurityContext)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.proxyId").value(proxyId))
        mockMvc
            .perform(
                get(COMPANY_PROXIES_BY_ID_ENDPOINT, proxyId)
                    .contentType(APPLICATION_JSON)
                    .with(securityContext(mockSecurityContext)),
            ).andExpect(status().isNotFound)
    }

    @Test
    fun `change existing company proxy using put request`() {
        val companyIdProxyCompany = createCompany()
        val companyIdProxiedCompany = createCompany()
        val createRequest = CompanyProxy(companyIdProxiedCompany, companyIdProxyCompany, "sfdr", "2024")
        val postResult =
            performProxyRequest("POST", COMPANY_PROXIES_ENDPOINT, null, createRequest)
                .andExpect(status().isOk)
        val proxyId = getProxyIdFromResult(postResult)
        val updateRequest = CompanyProxy(companyIdProxiedCompany, companyIdProxyCompany, "lksg", "2023")
        performProxyRequest("PUT", COMPANY_PROXIES_BY_ID_ENDPOINT, proxyId, updateRequest)
            .andExpect(status().isOk)
        mockMvc
            .perform(
                get(COMPANY_PROXIES_BY_ID_ENDPOINT, proxyId)
                    .contentType(APPLICATION_JSON)
                    .with(securityContext(mockSecurityContext)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath(JSON_PATH_PROXIED_COMPANY_ID).value(companyIdProxiedCompany))
            .andExpect(jsonPath(JSON_PATH_PROXY_COMPANY_ID).value(companyIdProxyCompany))
            .andExpect(jsonPath(JSON_PATH_FRAMEWORK).value("lksg"))
            .andExpect(jsonPath(JSON_PATH_REPORTING_PERIOD).value("2023"))
    }

    @Test
    fun `trying to create a proxy as a non admin user results in a 403`() {
        val companyIdProxyCompany = createCompany()
        val companyIdProxiedCompany = createCompany()
        val requestBody = CompanyProxy(companyIdProxiedCompany, companyIdProxyCompany, "sfdr", "2024")
        performProxyRequest("POST", COMPANY_PROXIES_ENDPOINT, null, requestBody, user = User.UPLOADER)
            .andExpect(status().isForbidden)
    }

    @Test
    fun `trying to delete a proxy as a non admin user results in a 403`() {
        val companyIdProxyCompany = createCompany()
        val companyIdProxiedCompany = createCompany()
        val requestBody = CompanyProxy(companyIdProxiedCompany, companyIdProxyCompany, "sfdr", "2024")
        val postResult =
            performProxyRequest("POST", COMPANY_PROXIES_ENDPOINT, null, requestBody)
                .andExpect(status().isOk)
        val proxyId = getProxyIdFromResult(postResult)
        setMockUser(User.UPLOADER)
        mockMvc
            .perform(
                delete(COMPANY_PROXIES_BY_ID_ENDPOINT, proxyId)
                    .contentType(APPLICATION_JSON)
                    .with(securityContext(mockSecurityContext)),
            ).andExpect(status().isForbidden)
    }

    @Test
    fun `trying to get a proxy relation by proxyId as a non admin user results in a 403`() {
        val companyIdProxyCompany = createCompany()
        val companyIdProxiedCompany = createCompany()
        val requestBody = CompanyProxy(companyIdProxiedCompany, companyIdProxyCompany, "sfdr", "2024")
        val postResult =
            performProxyRequest("POST", COMPANY_PROXIES_ENDPOINT, null, requestBody)
                .andExpect(status().isOk)
        val proxyId = getProxyIdFromResult(postResult)
        setMockUser(User.UPLOADER)
        mockMvc
            .perform(
                get(COMPANY_PROXIES_BY_ID_ENDPOINT, proxyId)
                    .contentType(APPLICATION_JSON)
                    .with(securityContext(mockSecurityContext)),
            ).andExpect(status().isForbidden)
    }

    @Test
    fun `creating a proxy with an invalid framework returns 400`() {
        val companyIdProxyCompany = createCompany()
        val companyIdProxiedCompany = createCompany()
        val invalidFrameworkRequest =
            CompanyProxy(companyIdProxiedCompany, companyIdProxyCompany, "NOT_A_REAL_FRAMEWORK", "2024")
        performProxyRequest("POST", COMPANY_PROXIES_ENDPOINT, null, invalidFrameworkRequest)
            .andExpect(status().isBadRequest)
    }
}
