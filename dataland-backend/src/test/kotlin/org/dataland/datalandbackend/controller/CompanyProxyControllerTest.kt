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
        private const val JSON_PATH_CHILD_COMPANY_ID = "\$.childCompanyId"
        private const val JSON_PATH_PARENT_COMPANY_ID = "\$.parentCompanyId"
        private const val JSON_PATH_UPLOADER_USER_ID = "\$.uploaderUserId"
        private const val JSON_PATH_UPLOAD_TIMESTAMP = "\$.uploadTimestamp"
        private const val JSON_PATH_VALID_FROM = "\$.validFromReportingPeriod"
        private const val JSON_PATH_VALID_TO = "\$.validToReportingPeriod"
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
        val companyIdParentCompany = createCompany()
        val companyIdChildCompany = createCompany()
        val requestBody = CompanyProxy(companyIdChildCompany, companyIdParentCompany, "2023", "2024")

        val postResult =
            performProxyRequest("POST", COMPANY_PROXIES_ENDPOINT, null, requestBody)
                .andExpect(status().isOk)
                .andExpect(jsonPath(JSON_PATH_CHILD_COMPANY_ID).value(companyIdChildCompany))
                .andExpect(jsonPath(JSON_PATH_PARENT_COMPANY_ID).value(companyIdParentCompany))
                .andExpect(jsonPath(JSON_PATH_VALID_FROM).value("2023"))
                .andExpect(jsonPath(JSON_PATH_VALID_TO).value("2024"))
                .andExpect(jsonPath(JSON_PATH_UPLOADER_USER_ID).value(adminUserId.toString()))
                .andExpect(jsonPath(JSON_PATH_UPLOAD_TIMESTAMP).isNumber)

        val proxyId = getProxyIdFromResult(postResult)
        mockMvc
            .perform(
                get(COMPANY_PROXIES_BY_ID_ENDPOINT, proxyId)
                    .contentType(APPLICATION_JSON)
                    .with(securityContext(mockSecurityContext)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.proxyId").value(proxyId))
            .andExpect(jsonPath(JSON_PATH_CHILD_COMPANY_ID).value(companyIdChildCompany))
            .andExpect(jsonPath(JSON_PATH_PARENT_COMPANY_ID).value(companyIdParentCompany))
            .andExpect(jsonPath(JSON_PATH_VALID_FROM).value("2023"))
            .andExpect(jsonPath(JSON_PATH_VALID_TO).value("2024"))
    }

    @Test
    fun `creating a proxy with invalid id returns an error`() {
        val invalidRequest = CompanyProxy("123", "456", "2022", "2023")
        performProxyRequest("POST", COMPANY_PROXIES_ENDPOINT, null, invalidRequest)
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `create proxy then delete proxy and assert that it is no longer retrievable`() {
        val companyIdParentCompany = createCompany()
        val companyIdChildCompany = createCompany()
        val requestBody = CompanyProxy(companyIdChildCompany, companyIdParentCompany, "2023", "2024")
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
        val companyIdParentCompany = createCompany()
        val companyIdChildCompany = createCompany()
        val createRequest = CompanyProxy(companyIdChildCompany, companyIdParentCompany, "2023", "2024")
        val postResult =
            performProxyRequest("POST", COMPANY_PROXIES_ENDPOINT, null, createRequest)
                .andExpect(status().isOk)
        val proxyId = getProxyIdFromResult(postResult)
        val updateRequest = CompanyProxy(companyIdChildCompany, companyIdParentCompany, "2021", "2022")
        performProxyRequest("PUT", COMPANY_PROXIES_BY_ID_ENDPOINT, proxyId, updateRequest)
            .andExpect(status().isOk)
            .andExpect(jsonPath(JSON_PATH_UPLOADER_USER_ID).value(adminUserId.toString()))
        mockMvc
            .perform(
                get(COMPANY_PROXIES_BY_ID_ENDPOINT, proxyId)
                    .contentType(APPLICATION_JSON)
                    .with(securityContext(mockSecurityContext)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath(JSON_PATH_CHILD_COMPANY_ID).value(companyIdChildCompany))
            .andExpect(jsonPath(JSON_PATH_PARENT_COMPANY_ID).value(companyIdParentCompany))
            .andExpect(jsonPath(JSON_PATH_VALID_FROM).value("2021"))
            .andExpect(jsonPath(JSON_PATH_VALID_TO).value("2022"))
    }

    @Test
    fun `trying to create a proxy as a non admin user results in a 403`() {
        val companyIdParentCompany = createCompany()
        val companyIdChildCompany = createCompany()
        val requestBody = CompanyProxy(companyIdChildCompany, companyIdParentCompany, "2023", "2024")
        performProxyRequest("POST", COMPANY_PROXIES_ENDPOINT, null, requestBody, user = User.UPLOADER)
            .andExpect(status().isForbidden)
    }

    @Test
    fun `trying to delete a proxy as a non admin user results in a 403`() {
        val companyIdParentCompany = createCompany()
        val companyIdChildCompany = createCompany()
        val requestBody = CompanyProxy(companyIdChildCompany, companyIdParentCompany, "2023", "2024")
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
        val companyIdParentCompany = createCompany()
        val companyIdChildCompany = createCompany()
        val requestBody = CompanyProxy(companyIdChildCompany, companyIdParentCompany, "2023", "2024")
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
    fun `creating a proxy with an invalid reporting period returns 400`() {
        val companyIdParentCompany = createCompany()
        val companyIdChildCompany = createCompany()
        val invalidPeriodRequest =
            CompanyProxy(companyIdChildCompany, companyIdParentCompany, "NOT_A_PERIOD", "2024")
        performProxyRequest("POST", COMPANY_PROXIES_ENDPOINT, null, invalidPeriodRequest)
            .andExpect(status().isBadRequest)
    }

    private fun createProxy(
        child: String,
        parent: String,
        from: String?,
        to: String?,
    ): String =
        getProxyIdFromResult(
            performProxyRequest("POST", COMPANY_PROXIES_ENDPOINT, null, CompanyProxy(child, parent, from, to))
                .andExpect(status().isOk),
        )

    @Test
    fun `creating a proxy with overlapping interval for the same child returns 400`() {
        val child = createCompany()
        createProxy(child, createCompany(), "2023", "2025")
        performProxyRequest("POST", COMPANY_PROXIES_ENDPOINT, null, CompanyProxy(child, createCompany(), "2025", null))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `creating a proxy with validFrom after validTo returns 400`() {
        performProxyRequest(
            "POST", COMPANY_PROXIES_ENDPOINT, null, CompanyProxy(createCompany(), createCompany(), "2025", "2023"),
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `creating a proxy whose parent is already a child returns 400`() {
        val parent = createCompany()
        val grandParent = createCompany()
        createProxy(parent, grandParent, "2023", "2024")
        performProxyRequest("POST", COMPANY_PROXIES_ENDPOINT, null, CompanyProxy(createCompany(), parent, "2023", "2024"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `creating a proxy whose child is already a parent is allowed`() {
        val parent = createCompany()
        createProxy(createCompany(), parent, "2023", "2024")
        performProxyRequest("POST", COMPANY_PROXIES_ENDPOINT, null, CompanyProxy(parent, createCompany(), "2023", "2024"))
            .andExpect(status().isOk)
    }

    @Test
    fun `creating a proxy with identical child and parent returns 400`() {
        val company = createCompany()
        performProxyRequest("POST", COMPANY_PROXIES_ENDPOINT, null, CompanyProxy(company, company, "2023", "2024"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `put that conflicts with another proxy of the same child returns 400`() {
        val child = createCompany()
        val parent = createCompany()
        createProxy(child, parent, "2023", "2024")
        val secondId = createProxy(child, parent, "2025", "2026")
        performProxyRequest("PUT", COMPANY_PROXIES_BY_ID_ENDPOINT, secondId, CompanyProxy(child, parent, "2024", "2026"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `put for an unknown proxy id returns 404`() {
        performProxyRequest(
            "PUT", COMPANY_PROXIES_BY_ID_ENDPOINT, UUID.randomUUID().toString(),
            CompanyProxy(createCompany(), createCompany(), "2023", "2024"),
        ).andExpect(status().isNotFound)
    }

    private fun searchProxies(
        child: String? = null,
        parent: String? = null,
        chunkSize: Int? = null,
        chunkIndex: Int? = null,
        user: User = User.ADMIN,
    ): ResultActions {
        setMockUser(user)
        val builder = get(COMPANY_PROXIES_ENDPOINT)
        child?.let { builder.param("childCompanyId", it) }
        parent?.let { builder.param("parentCompanyId", it) }
        chunkSize?.let { builder.param("chunkSize", it.toString()) }
        chunkIndex?.let { builder.param("chunkIndex", it.toString()) }
        return mockMvc.perform(builder.with(securityContext(mockSecurityContext)))
    }

    @Test
    fun `search proxies by child company id returns only matching proxies`() {
        val child = createCompany()
        val parent = createCompany()
        val matchingId = createProxy(child, parent, "2023", "2024")
        createProxy(createCompany(), parent, "2023", "2024")
        searchProxies(child = child)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].proxyId").value(matchingId))
            .andExpect(jsonPath("$[0].childCompanyId").value(child))
            .andExpect(jsonPath("$[0].parentCompanyId").value(parent))
            .andExpect(jsonPath("$[0].validFromReportingPeriod").value("2023"))
            .andExpect(jsonPath("$[0].validToReportingPeriod").value("2024"))
    }

    @Test
    fun `search proxies by parent company id returns all proxies of that parent`() {
        val parent = createCompany()
        val firstId = createProxy(createCompany(), parent, "2023", "2024")
        val secondId = createProxy(createCompany(), parent, "2023", "2024")
        createProxy(createCompany(), createCompany(), "2023", "2024")
        searchProxies(parent = parent)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[*].proxyId").value(org.hamcrest.Matchers.containsInAnyOrder(firstId, secondId)))
    }

    @Test
    fun `search proxies with child and parent filter combines both filters`() {
        val child = createCompany()
        val parent = createCompany()
        val matchingId = createProxy(child, parent, "2023", "2024")
        createProxy(child, createCompany(), "2025", "2026")
        searchProxies(child = child, parent = parent)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].proxyId").value(matchingId))
    }

    @Test
    fun `search proxies without filters includes created proxies`() {
        val proxyId = createProxy(createCompany(), createCompany(), "2023", "2024")
        searchProxies()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[*].proxyId").value(org.hamcrest.Matchers.hasItem(proxyId)))
    }

    @Test
    fun `search proxies returns empty list if nothing matches`() {
        searchProxies(child = createCompany())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    fun `search proxies respects chunkSize and chunkIndex`() {
        val child = createCompany()
        val parent = createCompany()
        createProxy(child, parent, "2021", "2022")
        createProxy(child, parent, "2023", "2024")
        createProxy(child, parent, "2025", "2026")
        searchProxies(child = child, chunkSize = 2, chunkIndex = 0)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
        searchProxies(child = child, chunkSize = 2, chunkIndex = 1)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
        searchProxies(child = child, chunkSize = 2, chunkIndex = 2)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    fun `trying to search proxies as a non admin user results in a 403`() {
        searchProxies(user = User.UPLOADER).andExpect(status().isForbidden)
    }

    @Test
    fun `search proxies with a malformed company id returns 400`() {
        searchProxies(child = "not-a-uuid").andExpect(status().isBadRequest)
        searchProxies(parent = "not-a-uuid").andExpect(status().isBadRequest)
    }

    @Test
    fun `get with a malformed proxy id returns 404`() {
        setMockUser(User.ADMIN)
        mockMvc
            .perform(get(COMPANY_PROXIES_BY_ID_ENDPOINT, "not-a-uuid").with(securityContext(mockSecurityContext)))
            .andExpect(status().isNotFound)
    }
}
