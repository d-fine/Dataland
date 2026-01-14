package org.dataland.datalandbackend.controller

import com.jayway.jsonpath.JsonPath
import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.entities.BasicCompanyInformation
import org.dataland.datalandbackend.exceptions.JOB_NOT_FOUND_SUMMARY
import org.dataland.datalandbackend.model.companies.CompanyIdentifierValidationResult
import org.dataland.datalandbackend.services.CompanyQueryManager
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

const val JSONPATHEXP = "$.errors[0].summary"

@SpringBootTest(
    classes = [DatalandBackend::class],
    properties = ["spring.profiles.active=nodb"],
)
@AutoConfigureMockMvc
@DefaultMocks
class DataExportControllerTest(
    @Autowired private val mockMvc: MockMvc,
) {
    @MockitoBean
    private lateinit var companyQueryManager: CompanyQueryManager

    private val mockSecurityContext = mock<SecurityContext>()

    private val testUserId = UUID.randomUUID()
    private val otherUserId = UUID.randomUUID().toString()

    private val userAuthentication =
        AuthenticationMock.mockJwtAuthentication(
            username = "testuser",
            userId = testUserId.toString(),
            roles = setOf(DatalandRealmRole.ROLE_USER),
        )

    private val otherUserAuthentication =
        AuthenticationMock.mockJwtAuthentication(
            username = "otheruser",
            userId = otherUserId,
            roles = setOf(DatalandRealmRole.ROLE_USER),
        )

    @BeforeEach
    fun setup() {
        reset(mockSecurityContext)
        setMockUser(userAuthentication)
    }

    private fun setMockCompany() {
        whenever(companyQueryManager.validateCompanyIdentifiers(any()))
            .doReturn(
                listOf(
                    CompanyIdentifierValidationResult(
                        identifier = "LEI",
                        companyInformation =
                            BasicCompanyInformation(
                                "comp-1",
                                "company name",
                                "Berlin",
                                "DE",
                                null, null,
                            ),
                    ),
                ),
            )
    }

    private fun setMockUser(authentication: DatalandJwtAuthentication) {
        doReturn(authentication).whenever(mockSecurityContext).authentication
        SecurityContextHolder.setContext(mockSecurityContext)
    }

    @Test
    fun `get export job state returns 404 for not existent job`() {
        val nonExistentJobId = UUID.randomUUID()

        mockMvc
            .perform(
                get("/export/state/{exportJobId}", nonExistentJobId)
                    .with(securityContext(mockSecurityContext)),
            ).andExpect(status().isNotFound)
            .andExpect(
                jsonPath(JSONPATHEXP)
                    .value(JOB_NOT_FOUND_SUMMARY),
            )
    }

    @Test
    fun `user cannot access other users export job`() {
        setMockCompany()
        val mvcResult =
            mockMvc
                .perform(
                    post("/data/eutaxonomy-non-financials/export-jobs")
                        .contentType("application/json")
                        .content("""{"companyIds": ["comp-1"], "reportingPeriods": ["2024"], "fileFormat": "CSV"}""")
                        .with(securityContext(mockSecurityContext)),
                ).andExpect(status().isOk)
                .andReturn()

        val responseBody = mvcResult.response.contentAsString
        val testJobId = JsonPath.parse(responseBody).read<String>("$.id")

        setMockUser(otherUserAuthentication)

        mockMvc
            .perform(
                get("/export/state/{exportJobId}", testJobId)
                    .with(securityContext(mockSecurityContext)),
            ).andExpect(status().isNotFound)
            .andExpect(
                jsonPath(JSONPATHEXP)
                    .value(JOB_NOT_FOUND_SUMMARY),
            )
    }

    @Test
    fun `export download returns 404 for non existent job`() {
        val nonExistentJobId = UUID.randomUUID()

        mockMvc
            .perform(
                get("/export/download/{exportJobId}", nonExistentJobId)
                    .with(securityContext(mockSecurityContext)),
            ).andExpect(status().isNotFound)
            .andExpect(
                jsonPath(JSONPATHEXP)
                    .value(JOB_NOT_FOUND_SUMMARY),
            )
    }
}
