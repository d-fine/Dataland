package org.dataland.datalandbackend.controller

import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.asResponseBody
import okio.Buffer
import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.model.proxies.CompanyProxy
import org.dataland.datalandbackend.utils.DefaultMocks
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
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
        val dummyBody = Buffer().asResponseBody(null, 0)
        val response =
            Response
                .Builder()
                .request(Request.Builder().url("http://localhost/dummy").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(dummyBody)
                .build()
        whenever(authenticatedOkHttpClient.newCall(any())).thenReturn(mockCall)
        whenever(mockCall.execute()).thenReturn(response)
    }

    private fun createCompany(): String {
        setMockUser(User.ADMIN)
        val lei =
            UUID
                .randomUUID()
                .toString()
                .replace("-", "")
                .take(20)
        val companyRequest =
            mapOf(
                "companyName" to "ABC Corporation $lei",
                "headquarters" to "Berlin",
                "identifiers" to mapOf("Lei" to listOf(lei)),
                "countryCode" to "DE",
            )
        val result =
            mockMvc
                .perform(
                    post(COMPANIES_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .content(defaultObjectMapper.writeValueAsString(companyRequest))
                        .with(securityContext(mockSecurityContext)),
                ).andExpect(status().isOk)
                .andReturn()
        return defaultObjectMapper.readTree(result.response.contentAsString).get(COMPANY_ID_JSON_FIELD).asText()
    }

    private fun perform(
        builder: MockHttpServletRequestBuilder,
        user: User = User.ADMIN,
    ): ResultActions {
        setMockUser(user)
        return mockMvc.perform(builder.with(securityContext(mockSecurityContext)))
    }

    private fun postProxy(
        child: String,
        parent: String,
        from: String? = "2023",
        to: String? = "2024",
        user: User = User.ADMIN,
    ): ResultActions =
        perform(
            post(COMPANY_PROXIES_ENDPOINT)
                .contentType(APPLICATION_JSON)
                .content(defaultObjectMapper.writeValueAsString(CompanyProxy(child, parent, from, to))),
            user,
        )

    private fun putProxy(
        proxyId: String,
        child: String,
        parent: String,
        from: String? = "2023",
        to: String? = "2024",
    ): ResultActions =
        perform(
            put(COMPANY_PROXIES_BY_ID_ENDPOINT, proxyId)
                .contentType(APPLICATION_JSON)
                .content(defaultObjectMapper.writeValueAsString(CompanyProxy(child, parent, from, to))),
        )

    private fun getProxy(
        proxyId: String,
        user: User = User.ADMIN,
    ): ResultActions = perform(get(COMPANY_PROXIES_BY_ID_ENDPOINT, proxyId), user)

    private fun deleteProxy(
        proxyId: String,
        user: User = User.ADMIN,
    ): ResultActions = perform(delete(COMPANY_PROXIES_BY_ID_ENDPOINT, proxyId), user)

    private fun getProxyIdFromResult(result: ResultActions): String =
        defaultObjectMapper.readTree(result.andReturn().response.contentAsString)["proxyId"].asText()

    private fun getProxyIdsFromSearchResult(result: ResultActions): List<String> =
        defaultObjectMapper.readTree(result.andReturn().response.contentAsString).map { it["proxyId"].asText() }

    private fun createProxy(
        child: String,
        parent: String,
        from: String? = "2023",
        to: String? = "2024",
    ): String = getProxyIdFromResult(postProxy(child, parent, from, to).andExpect(status().isOk))

    private fun ResultActions.expectProxy(
        child: String,
        parent: String,
        from: String,
        to: String,
    ): ResultActions =
        andExpect(status().isOk)
            .andExpect(jsonPath(JSON_PATH_CHILD_COMPANY_ID).value(child))
            .andExpect(jsonPath(JSON_PATH_PARENT_COMPANY_ID).value(parent))
            .andExpect(jsonPath(JSON_PATH_VALID_FROM).value(from))
            .andExpect(jsonPath(JSON_PATH_VALID_TO).value(to))

    @Test
    fun `create proxy then get by id returns correct data`() {
        val parent = createCompany()
        val child = createCompany()
        val postResult =
            postProxy(child, parent)
                .expectProxy(child, parent, "2023", "2024")
                .andExpect(jsonPath(JSON_PATH_UPLOADER_USER_ID).value(adminUserId.toString()))
                .andExpect(jsonPath(JSON_PATH_UPLOAD_TIMESTAMP).isNumber)
        val proxyId = getProxyIdFromResult(postResult)
        getProxy(proxyId)
            .expectProxy(child, parent, "2023", "2024")
            .andExpect(jsonPath("$.proxyId").value(proxyId))
    }

    @Test
    fun `invalid proxy creation requests return 400`() {
        postProxy("123", "456", "2022", "2023").andExpect(status().isBadRequest)
        postProxy(createCompany(), createCompany(), "NOT_A_PERIOD", "2024").andExpect(status().isBadRequest)
        postProxy(createCompany(), createCompany(), "2025", "2023").andExpect(status().isBadRequest)
        val company = createCompany()
        postProxy(company, company).andExpect(status().isBadRequest)
    }

    @Test
    fun `create proxy then delete proxy and assert that it is no longer retrievable`() {
        val proxyId = createProxy(createCompany(), createCompany())
        deleteProxy(proxyId).andExpect(status().isOk).andExpect(jsonPath("$.proxyId").value(proxyId))
        getProxy(proxyId).andExpect(status().isNotFound)
    }

    @Test
    fun `change existing company proxy using put request`() {
        val parent = createCompany()
        val child = createCompany()
        val proxyId = createProxy(child, parent)
        putProxy(proxyId, child, parent, "2021", "2022")
            .andExpect(status().isOk)
            .andExpect(jsonPath(JSON_PATH_UPLOADER_USER_ID).value(adminUserId.toString()))
        getProxy(proxyId).expectProxy(child, parent, "2021", "2022")
    }

    @Test
    fun `non admin users get a 403 when creating, deleting, getting or searching proxies`() {
        val child = createCompany()
        val parent = createCompany()
        postProxy(child, parent, user = User.UPLOADER).andExpect(status().isForbidden)
        val proxyId = createProxy(child, parent)
        deleteProxy(proxyId, User.UPLOADER).andExpect(status().isForbidden)
        getProxy(proxyId, User.UPLOADER).andExpect(status().isForbidden)
        searchProxies(user = User.UPLOADER).andExpect(status().isForbidden)
    }

    @Test
    fun `creating a proxy with overlapping interval for the same child returns 400`() {
        val child = createCompany()
        createProxy(child, createCompany(), "2023", "2025")
        postProxy(child, createCompany(), "2025", null).andExpect(status().isBadRequest)
    }

    @Test
    fun `creating a proxy whose parent is already a child returns 400`() {
        val parent = createCompany().also { createProxy(it, createCompany()) }
        postProxy(createCompany(), parent).andExpect(status().isBadRequest)
    }

    @Test
    fun `creating a proxy whose child is already a parent is allowed`() {
        val parent = createCompany().also { createProxy(createCompany(), it) }
        postProxy(parent, createCompany()).andExpect(status().isOk)
    }

    @Test
    fun `put that conflicts with another proxy of the same child returns 400`() {
        val child = createCompany()
        val parent = createCompany()
        createProxy(child, parent, "2023", "2024")
        val secondId = createProxy(child, parent, "2025", "2026")
        putProxy(secondId, child, parent, "2024", "2026").andExpect(status().isBadRequest)
    }

    @Test
    fun `put for an unknown proxy id returns 404`() {
        putProxy(UUID.randomUUID().toString(), createCompany(), createCompany()).andExpect(status().isNotFound)
    }

    private fun searchProxies(
        child: String? = null,
        parent: String? = null,
        chunkSize: Int? = null,
        chunkIndex: Int? = null,
        user: User = User.ADMIN,
    ): ResultActions {
        val builder = get(COMPANY_PROXIES_ENDPOINT)
        child?.let { builder.param("childCompanyId", it) }
        parent?.let { builder.param("parentCompanyId", it) }
        chunkSize?.let { builder.param("chunkSize", it.toString()) }
        chunkIndex?.let { builder.param("chunkIndex", it.toString()) }
        return perform(builder, user)
    }

    @Test
    fun `search proxies by child company id returns only matching proxies`() {
        val child = createCompany()
        val parent = createCompany()
        val matchingId = createProxy(child, parent)
        createProxy(createCompany(), parent)
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
        val firstId = createProxy(createCompany(), parent)
        val secondId = createProxy(createCompany(), parent)
        createProxy(createCompany(), createCompany())
        val result =
            searchProxies(parent = parent)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.length()").value(2))
        assertEquals(setOf(firstId, secondId), getProxyIdsFromSearchResult(result).toSet())
    }

    @Test
    fun `search proxies with child and parent filter combines both filters`() {
        val child = createCompany()
        val parent = createCompany()
        val matchingId = createProxy(child, parent)
        createProxy(child, createCompany(), "2025", "2026")
        searchProxies(child = child, parent = parent)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].proxyId").value(matchingId))
    }

    @Test
    fun `search proxies returns empty list if nothing matches`() {
        searchProxies(child = createCompany())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    fun `search proxies without filters includes created proxies`() {
        val proxyId = createProxy(createCompany(), createCompany())
        val result = searchProxies().andExpect(status().isOk)
        assertTrue(proxyId in getProxyIdsFromSearchResult(result))
    }

    @Test
    fun `search proxies respects chunkSize and chunkIndex`() {
        val child = createCompany()
        val parent = createCompany()
        createProxy(child, parent, "2021", "2022")
        createProxy(child, parent, "2023", "2024")
        createProxy(child, parent, "2025", "2026")
        listOf(0 to 2, 1 to 1, 2 to 0).forEach { (index, expected) ->
            searchProxies(child = child, chunkSize = 2, chunkIndex = index)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.length()").value(expected))
        }
    }

    @Test
    fun `search proxies with a malformed company id returns 400`() {
        searchProxies(child = "not-a-uuid").andExpect(status().isBadRequest)
        searchProxies(parent = "not-a-uuid").andExpect(status().isBadRequest)
    }

    @Test
    fun `get with a malformed proxy id returns 404`() = getProxy("not-a-uuid").andExpect(status().isNotFound).let {}
}
