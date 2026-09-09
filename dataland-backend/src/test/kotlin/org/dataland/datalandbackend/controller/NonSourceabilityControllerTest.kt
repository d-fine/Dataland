package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.companies.CompanyInformation
import org.dataland.datalandbackend.model.metainformation.NonSourceabilityRequest
import org.dataland.datalandbackend.services.CompanyAlterationManager
import org.dataland.datalandbackend.services.NonSourceabilityInformationManager
import org.dataland.datalandbackend.utils.DefaultMocks
import org.dataland.datalandbackendutils.model.BasicDataDimensions
import org.dataland.datalandbackendutils.utils.JsonUtils
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

private const val SEARCH_NON_SOURCEABLE_PATH = "/non-sourceable/search"
private const val SEARCH_NON_SOURCEABLE_GROUPED_PATH = "/non-sourceable/search/grouped"
private const val CONTENT_TYPE = "application/json"

/**
 * Endpoint test for POST /non-sourceable/search and POST /non-sourceable/search/grouped, exercising the full stack
 * (controller -> NonSourceabilityInformationManager -> NonSourceabilityDataRepository -> H2)
 * without mocking the business service layer. Only the CloudEventMessageHandler (RabbitMQ
 * side effect) is mocked, matching the pattern used in MetaDataControllerNonSourceableTest.
 *
 * Response bodies are deserialized back into their Kotlin target types before asserting on them,
 * rather than navigating the raw JSON via jsonPath. This still validates the actual serialized
 * response bytes (via the real ObjectMapper), while keeping the assertions readable and
 * order-independent (important since several of the response types are Kotlin Sets).
 */
@SpringBootTest(
    classes = [DatalandBackend::class],
    properties = ["spring.profiles.active=nodb"],
)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
@DefaultMocks
@MockitoBean(types = [CloudEventMessageHandler::class])
class NonSourceabilityControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val companyAlterationManager: CompanyAlterationManager,
    @Autowired private val nonSourceabilityInformationManager: NonSourceabilityInformationManager,
) {
    private val objectMapper = JsonUtils.defaultObjectMapper

    private lateinit var storedCompany: StoredCompanyEntity
    private val adminRoles = DatalandRealmRole.entries.toSet()
    private val dataType = DataType("sfdr")
    private val reportingPeriod = "2023"

    private val mockSecurityContext = mock<SecurityContext>()

    private val userAuthentication =
        AuthenticationMock.mockJwtAuthentication(
            username = "testuser",
            userId = "test-user-id",
            roles = setOf(DatalandRealmRole.ROLE_USER),
        )

    @BeforeEach
    fun setup() {
        AuthenticationMock
            .mockSecurityContext("uploader", "uploaderId", setOf(DatalandRealmRole.ROLE_USER, DatalandRealmRole.ROLE_UPLOADER))
        val companyInfo =
            CompanyInformation(
                companyName = "TestCo",
                headquarters = "DE",
                headquartersPostalCode = "10115",
                countryCode = "DE",
                companyContactDetails = emptyList(),
                companyLegalForm = null,
                sector = null,
                website = null,
                identifiers = emptyMap(),
                companyAlternativeNames = null,
                isTeaserCompany = false,
                parentCompanyLei = null,
            )
        storedCompany = companyAlterationManager.addCompany(companyInfo)

        reset(mockSecurityContext)
        doReturn(userAuthentication).whenever(mockSecurityContext).authentication
        SecurityContextHolder.setContext(mockSecurityContext)
    }

    private fun markNonSourceable(
        period: String = reportingPeriod,
        type: DataType = dataType,
        currentlyActive: Boolean = true,
    ) {
        AuthenticationMock.mockSecurityContext("admin", "adminId", adminRoles)
        nonSourceabilityInformationManager.processNonSourceabilityRequest(
            NonSourceabilityRequest(
                companyId = storedCompany.companyId,
                dataType = type,
                reportingPeriod = period,
                reason = "No public source",
            ),
            bypassQa = true,
            currentlyActive = currentlyActive,
        )
    }

    private fun searchRequestBody(
        companyIds: String = """["${storedCompany.companyId}"]""",
        dataTypes: String = """["${dataType.name}"]""",
        reportingPeriods: String = """["$reportingPeriod"]""",
    ) = """
        {
            "companyIds": $companyIds,
            "dataTypes": $dataTypes,
            "reportingPeriods": $reportingPeriods
        }
        """.trimIndent()

    private fun performSearch(requestBody: String = searchRequestBody()) =
        mockMvc
            .perform(
                post(SEARCH_NON_SOURCEABLE_PATH)
                    .contentType(CONTENT_TYPE)
                    .content(requestBody)
                    .with(securityContext(mockSecurityContext)),
            )

    private fun performGroupedSearch(requestBody: String = searchRequestBody()) =
        mockMvc
            .perform(
                post(SEARCH_NON_SOURCEABLE_GROUPED_PATH)
                    .contentType(CONTENT_TYPE)
                    .content(requestBody)
                    .with(securityContext(mockSecurityContext)),
            )

    private fun readSearchResponse(responseBody: String): Set<BasicDataDimensions> = objectMapper.readValue(responseBody)

    private fun readGroupedSearchResponse(responseBody: String): Map<String, Map<String, Set<BasicDataDimensions>>> =
        objectMapper.readValue(responseBody)

    @Test
    fun `search returns the active non-sourceable triple matching filters`() {
        markNonSourceable()

        val response = performSearch().andExpect(status().isOk).andReturn().response
        val result = readSearchResponse(response.contentAsString)

        assertEquals(setOf(BasicDataDimensions(storedCompany.companyId, dataType.name, reportingPeriod)), result)
    }

    @Test
    fun `search treats empty lists as wildcards`() {
        markNonSourceable()

        val response =
            performSearch(searchRequestBody(companyIds = "[]", dataTypes = "[]", reportingPeriods = "[]"))
                .andExpect(status().isOk)
                .andReturn()
                .response
        val result = readSearchResponse(response.contentAsString)

        assertEquals(setOf(BasicDataDimensions(storedCompany.companyId, dataType.name, reportingPeriod)), result)
    }

    @Test
    fun `search excludes pending entries`() {
        AuthenticationMock
            .mockSecurityContext("uploader", "uploaderId", setOf(DatalandRealmRole.ROLE_USER, DatalandRealmRole.ROLE_UPLOADER))
        nonSourceabilityInformationManager.processNonSourceabilityRequest(
            NonSourceabilityRequest(
                companyId = storedCompany.companyId,
                dataType = dataType,
                reportingPeriod = reportingPeriod,
                reason = "Pending request",
            ),
            bypassQa = false,
            currentlyActive = false,
        )

        val response = performSearch().andExpect(status().isOk).andReturn().response
        val result = readSearchResponse(response.contentAsString)

        assertEquals(emptySet<BasicDataDimensions>(), result)
    }

    @Test
    fun `search excludes reversed entries`() {
        markNonSourceable()
        AuthenticationMock.mockSecurityContext("admin", "adminId", adminRoles)
        nonSourceabilityInformationManager.processNonSourceabilityRequest(
            NonSourceabilityRequest(
                companyId = storedCompany.companyId,
                dataType = dataType,
                reportingPeriod = reportingPeriod,
                reason = "Reversal",
            ),
            bypassQa = true,
            currentlyActive = false,
        )

        val response = performSearch().andExpect(status().isOk).andReturn().response
        val result = readSearchResponse(response.contentAsString)

        assertEquals(emptySet<BasicDataDimensions>(), result)
    }

    @Test
    fun `search returns 401 for unauthenticated request`() {
        mockMvc
            .perform(
                post(SEARCH_NON_SOURCEABLE_PATH)
                    .contentType(CONTENT_TYPE)
                    .content(searchRequestBody()),
            ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `grouped search returns the active non-sourceable triple matching filters`() {
        markNonSourceable()

        val response = performGroupedSearch().andExpect(status().isOk).andReturn().response
        val result = readGroupedSearchResponse(response.contentAsString)

        assertEquals(
            mapOf(
                storedCompany.companyId to
                    mapOf(dataType.name to setOf(BasicDataDimensions(storedCompany.companyId, dataType.name, reportingPeriod))),
            ),
            result,
        )
    }

    @Test
    fun `grouped search treats empty lists as wildcards`() {
        markNonSourceable()

        val response =
            performGroupedSearch(searchRequestBody(companyIds = "[]", dataTypes = "[]", reportingPeriods = "[]"))
                .andExpect(status().isOk)
                .andReturn()
                .response
        val result = readGroupedSearchResponse(response.contentAsString)

        assertEquals(
            mapOf(
                storedCompany.companyId to
                    mapOf(dataType.name to setOf(BasicDataDimensions(storedCompany.companyId, dataType.name, reportingPeriod))),
            ),
            result,
        )
    }

    @Test
    fun `grouped search excludes pending entries`() {
        AuthenticationMock
            .mockSecurityContext("uploader", "uploaderId", setOf(DatalandRealmRole.ROLE_USER, DatalandRealmRole.ROLE_UPLOADER))
        nonSourceabilityInformationManager.processNonSourceabilityRequest(
            NonSourceabilityRequest(
                companyId = storedCompany.companyId,
                dataType = dataType,
                reportingPeriod = reportingPeriod,
                reason = "Pending request",
            ),
            bypassQa = false,
            currentlyActive = false,
        )

        val response = performGroupedSearch().andExpect(status().isOk).andReturn().response
        val result = readGroupedSearchResponse(response.contentAsString)

        assertEquals(emptyMap<String, Map<String, Set<BasicDataDimensions>>>(), result)
    }

    @Test
    fun `grouped search excludes reversed entries`() {
        markNonSourceable()
        AuthenticationMock.mockSecurityContext("admin", "adminId", adminRoles)
        nonSourceabilityInformationManager.processNonSourceabilityRequest(
            NonSourceabilityRequest(
                companyId = storedCompany.companyId,
                dataType = dataType,
                reportingPeriod = reportingPeriod,
                reason = "Reversal",
            ),
            bypassQa = true,
            currentlyActive = false,
        )

        val response = performGroupedSearch().andExpect(status().isOk).andReturn().response
        val result = readGroupedSearchResponse(response.contentAsString)

        assertEquals(emptyMap<String, Map<String, Set<BasicDataDimensions>>>(), result)
    }

    @Test
    fun `grouped search returns 401 for unauthenticated request`() {
        mockMvc
            .perform(
                post(SEARCH_NON_SOURCEABLE_GROUPED_PATH)
                    .contentType(CONTENT_TYPE)
                    .content(searchRequestBody()),
            ).andExpect(status().isUnauthorized)
    }
}
