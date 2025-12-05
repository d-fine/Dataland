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
import org.dataland.datalandbackend.services.SpecificationService
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
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
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
class CompanyProxyControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
) {
    @Suppress("UnusedPrivateMember") // Prevents real SpecificationService startup logic
    @MockitoBean
    private lateinit var specificationService: SpecificationService

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

    @BeforeEach
    fun setup() {
        reset(mockSecurityContext, authenticatedOkHttpClient)
        setupCompanyExistsValidatorMocks()
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
        setMockSecurityContext(adminAuthentication)

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
                    post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(companyRequestJson)
                        .with(securityContext(mockSecurityContext)),
                ).andExpect(status().isOk)
                .andReturn()

        return objectMapper
            .readTree(result.response.contentAsString)
            .get("companyId")
            .asText()
    }

    @Test
    fun `create proxy then get by id returns correct data`() {
        val companyIdProxyCompany = createCompany()
        val companyIdProxiedCompany = createCompany()

        setMockSecurityContext(adminAuthentication)

        val requestBody =
            CompanyProxy(
                proxiedCompanyId = companyIdProxiedCompany,
                proxyCompanyId = companyIdProxyCompany,
                framework = "sfdr",
                reportingPeriod = "2024",
            )

        val postResult =
            mockMvc
                .perform(
                    post("/company-proxies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody))
                        .with(securityContext(mockSecurityContext)),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.proxiedCompanyId").value(companyIdProxiedCompany))
                .andExpect(jsonPath("$.proxyCompanyId").value(companyIdProxyCompany))
                .andExpect(jsonPath("$.framework").value("sfdr"))
                .andExpect(jsonPath("$.reportingPeriod").value("2024"))
                .andReturn()

        val proxyId =
            objectMapper.readTree(postResult.response.contentAsString)["proxyId"].asText()

        mockMvc
            .perform(
                get("/company-proxies/{proxyId}", proxyId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(securityContext(mockSecurityContext)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.proxyId").value(proxyId))
            .andExpect(jsonPath("$.proxiedCompanyId").value(companyIdProxiedCompany))
            .andExpect(jsonPath("$.proxyCompanyId").value(companyIdProxyCompany))
            .andExpect(jsonPath("$.framework").value("sfdr"))
            .andExpect(jsonPath("$.reportingPeriod").value("2024"))
    }

    @Test
    fun `creating a proxy with invalid inputs returns an error`() {
        setMockSecurityContext(adminAuthentication)

        val invalidRequest =
            CompanyProxy(
                proxiedCompanyId = "123",
                proxyCompanyId = "456",
                framework = "lksg",
                reportingPeriod = "2023",
            )

        mockMvc
            .perform(
                post("/company-proxies")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest))
                    .with(securityContext(mockSecurityContext)),
            ).andExpect(status().isBadRequest)
    }

    @Test
    fun `create proxy then delete proxy and assert that it is no longer retrievable`() {
        val companyIdProxyCompany = createCompany()
        val companyIdProxiedCompany = createCompany()
        setMockSecurityContext(adminAuthentication)

        val requestBody =
            CompanyProxy(
                proxiedCompanyId = companyIdProxiedCompany,
                proxyCompanyId = companyIdProxyCompany,
                framework = "sfdr",
                reportingPeriod = "2024",
            )

        val postResult =
            mockMvc
                .perform(
                    post("/company-proxies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody))
                        .with(securityContext(mockSecurityContext)),
                ).andExpect(status().isOk)
                .andReturn()

        val proxyId =
            objectMapper.readTree(postResult.response.contentAsString)["proxyId"].asText()

        mockMvc
            .perform(
                delete("/company-proxies/{proxyId}", proxyId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(securityContext(mockSecurityContext)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.proxyId").value(proxyId))

        mockMvc
            .perform(
                get("/company-proxies/{proxyId}", proxyId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(securityContext(mockSecurityContext)),
            ).andExpect(status().isNotFound)
    }

    @Test
    fun `change existing company proxy using put request`() {
        val companyIdProxyCompany = createCompany()
        val companyIdProxiedCompany = createCompany()
        setMockSecurityContext(adminAuthentication)

        val createRequest =
            CompanyProxy(
                proxiedCompanyId = companyIdProxiedCompany,
                proxyCompanyId = companyIdProxyCompany,
                framework = "sfdr",
                reportingPeriod = "2024",
            )

        val postResult =
            mockMvc
                .perform(
                    post("/company-proxies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest))
                        .with(securityContext(mockSecurityContext)),
                ).andExpect(status().isOk)
                .andReturn()

        val proxyId =
            objectMapper.readTree(postResult.response.contentAsString)["proxyId"].asText()

        val updateRequest =
            CompanyProxy(
                proxiedCompanyId = companyIdProxiedCompany,
                proxyCompanyId = companyIdProxyCompany,
                framework = "lksg",
                reportingPeriod = "2023",
            )

        mockMvc
            .perform(
                put("/company-proxies/{proxyId}", proxyId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest))
                    .with(securityContext(mockSecurityContext)),
            ).andExpect(status().isOk)

        mockMvc
            .perform(
                get("/company-proxies/{proxyId}", proxyId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(securityContext(mockSecurityContext)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.proxiedCompanyId").value(companyIdProxiedCompany))
            .andExpect(jsonPath("$.proxyCompanyId").value(companyIdProxyCompany))
            .andExpect(jsonPath("$.framework").value("lksg"))
            .andExpect(jsonPath("$.reportingPeriod").value("2023"))
    }

    @Test
    fun `trying to create a proxy as a non admin user results in a 403`() {
        val companyIdProxyCompany = createCompany()
        val companyIdProxiedCompany = createCompany()

        setMockSecurityContext(uploaderAuthentication)

        val requestBody =
            CompanyProxy(
                proxiedCompanyId = companyIdProxiedCompany,
                proxyCompanyId = companyIdProxyCompany,
                framework = "sfdr",
                reportingPeriod = "2024",
            )

        mockMvc
            .perform(
                post("/company-proxies")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody))
                    .with(securityContext(mockSecurityContext)),
            ).andExpect(status().isForbidden)
    }

    @Test
    fun `trying to delete a proxy as a non admin user results in a 403`() {
        val companyIdProxyCompany = createCompany()
        val companyIdProxiedCompany = createCompany()

        setMockSecurityContext(adminAuthentication)

        val requestBody =
            CompanyProxy(
                proxiedCompanyId = companyIdProxiedCompany,
                proxyCompanyId = companyIdProxyCompany,
                framework = "sfdr",
                reportingPeriod = "2024",
            )

        val postResult =
            mockMvc
                .perform(
                    post("/company-proxies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody))
                        .with(securityContext(mockSecurityContext)),
                ).andExpect(status().isOk)
                .andReturn()

        val proxyId =
            objectMapper.readTree(postResult.response.contentAsString)["proxyId"].asText()

        setMockSecurityContext(uploaderAuthentication)

        mockMvc
            .perform(
                delete("/company-proxies/{proxyId}", proxyId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(securityContext(mockSecurityContext)),
            ).andExpect(status().isForbidden)
    }

    @Test
    fun `trying to get a proxy relation by proxyId as a non admin user results in a 403`() {
        val companyIdProxyCompany = createCompany()
        val companyIdProxiedCompany = createCompany()

        setMockSecurityContext(adminAuthentication)

        val requestBody =
            CompanyProxy(
                proxiedCompanyId = companyIdProxiedCompany,
                proxyCompanyId = companyIdProxyCompany,
                framework = "sfdr",
                reportingPeriod = "2024",
            )

        val postResult =
            mockMvc
                .perform(
                    post("/company-proxies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody))
                        .with(securityContext(mockSecurityContext)),
                ).andExpect(status().isOk)
                .andReturn()

        val proxyId =
            objectMapper.readTree(postResult.response.contentAsString)["proxyId"].asText()

        setMockSecurityContext(uploaderAuthentication)

        mockMvc
            .perform(
                get("/company-proxies/{proxyId}", proxyId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(securityContext(mockSecurityContext)),
            ).andExpect(status().isForbidden)
    }

    @Test
    fun `creating a proxy with an invalid framework returns 400`() {
        val companyIdProxyCompany = createCompany()
        val companyIdProxiedCompany = createCompany()
        setMockSecurityContext(adminAuthentication)

        val invalidFrameworkRequest =
            CompanyProxy(
                proxiedCompanyId = companyIdProxiedCompany,
                proxyCompanyId = companyIdProxyCompany,
                framework = "NOT_A_REAL_FRAMEWORK",
                reportingPeriod = "2024",
            )

        mockMvc
            .perform(
                post("/company-proxies")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidFrameworkRequest))
                    .with(securityContext(mockSecurityContext)),
            ).andExpect(status().isBadRequest)
    }
}
