package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.model.DataDimensionQuery
import org.dataland.datalandbackend.services.DataAvailabilityChecker
import org.dataland.datalandbackend.utils.DefaultMocks
import org.dataland.datalandbackendutils.model.BasicDataDimensions
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

private const val FILTER_VIEWABLE_DIMENSIONS_PATH = "/data-availability/viewable-dimensions/filter"
private const val SEARCH_VIEWABLE_DIMENSIONS_PATH = "/data-availability/viewable-dimensions/search"

@SpringBootTest(
    classes = [DatalandBackend::class],
    properties = ["spring.profiles.active=nodb"],
)
@AutoConfigureMockMvc
@DefaultMocks
class DataAvailabilityControllerTest(
    @Autowired private val mockMvc: MockMvc,
) {
    @MockitoBean
    private lateinit var dataAvailabilityChecker: DataAvailabilityChecker

    private val mockSecurityContext = mock<SecurityContext>()

    private val userAuthentication =
        AuthenticationMock.mockJwtAuthentication(
            username = "testuser",
            userId = "test-user-id",
            roles = setOf(DatalandRealmRole.ROLE_USER),
        )

    private val exampleDimension =
        BasicDataDimensions(
            companyId = "test-company-id",
            dataType = "sfdr",
            reportingPeriod = "2023",
        )

    @BeforeEach
    fun setup() {
        reset(mockSecurityContext)
        doReturn(userAuthentication).whenever(mockSecurityContext).authentication
        SecurityContextHolder.setContext(mockSecurityContext)
    }

    // --- POST /data-availability/viewable-dimensions/filter ---

    @Test
    fun `filterViewableDimensions returns matched dimensions from checker`() {
        whenever(dataAvailabilityChecker.filterViewableDimensions(any<List<BasicDataDimensions>>()))
            .doReturn(listOf(exampleDimension))

        mockMvc
            .perform(
                post(FILTER_VIEWABLE_DIMENSIONS_PATH)
                    .contentType("application/json")
                    .content("""[{"companyId":"test-company-id","dataType":"sfdr","reportingPeriod":"2023"}]""")
                    .with(securityContext(mockSecurityContext)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$[0].companyId").value("test-company-id"))
            .andExpect(jsonPath("$[0].dataType").value("sfdr"))
            .andExpect(jsonPath("$[0].reportingPeriod").value("2023"))
    }

    @Test
    fun `filterViewableDimensions with empty list returns empty result`() {
        whenever(dataAvailabilityChecker.filterViewableDimensions(eq(emptyList<BasicDataDimensions>())))
            .doReturn(emptyList())

        mockMvc
            .perform(
                post(FILTER_VIEWABLE_DIMENSIONS_PATH)
                    .contentType("application/json")
                    .content("[]")
                    .with(securityContext(mockSecurityContext)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$").isEmpty)
    }

    @Test
    fun `filterViewableDimensions returns 401 for unauthenticated request`() {
        mockMvc
            .perform(
                post(FILTER_VIEWABLE_DIMENSIONS_PATH)
                    .contentType("application/json")
                    .content("[]"),
            ).andExpect(status().isUnauthorized)
    }

    // --- POST /data-availability/viewable-dimensions/search ---

    @Test
    fun `searchViewableDimensions returns matched dimensions from checker`() {
        whenever(
            dataAvailabilityChecker.searchViewableDimensions(any<DataDimensionQuery>()),
        ).doReturn(listOf(exampleDimension))

        mockMvc
            .perform(
                post(SEARCH_VIEWABLE_DIMENSIONS_PATH)
                    .contentType("application/json")
                    .content(
                        """
                        {
                            "companyIds": ["test-company-id"],
                            "frameworksOrDataPointTypes": ["sfdr"],
                            "reportingPeriods": ["2023"]
                        }
                        """.trimIndent(),
                    ).with(securityContext(mockSecurityContext)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$[0].companyId").value("test-company-id"))
            .andExpect(jsonPath("$[0].dataType").value("sfdr"))
            .andExpect(jsonPath("$[0].reportingPeriod").value("2023"))
    }

    @Test
    fun `searchViewableDimensions accepts empty companyIds as wildcard`() {
        whenever(
            dataAvailabilityChecker.searchViewableDimensions(any<DataDimensionQuery>()),
        ).doReturn(listOf(exampleDimension))

        mockMvc
            .perform(
                post(SEARCH_VIEWABLE_DIMENSIONS_PATH)
                    .contentType("application/json")
                    .content(
                        """
                        {
                            "companyIds": [],
                            "frameworksOrDataPointTypes": ["sfdr"],
                            "reportingPeriods": ["2023"]
                        }
                        """.trimIndent(),
                    ).with(securityContext(mockSecurityContext)),
            ).andExpect(status().isOk)
    }

    @Test
    fun `searchViewableDimensions returns 400 when the search filter is empty`() {
        whenever(
            dataAvailabilityChecker.searchViewableDimensions(any<DataDimensionQuery>()),
        ).doReturn(listOf(exampleDimension))

        mockMvc
            .perform(
                post(SEARCH_VIEWABLE_DIMENSIONS_PATH)
                    .contentType("application/json")
                    .content(
                        """
                        {
                            "frameworksOrDataPointTypes": [],
                            "reportingPeriods": []
                        }
                        """.trimIndent(),
                    ).with(securityContext(mockSecurityContext)),
            ).andExpect(status().isBadRequest)
    }

    @Test
    fun `searchViewableDimensions accepts empty reportingPeriods as wildcard`() {
        whenever(
            dataAvailabilityChecker.searchViewableDimensions(any<DataDimensionQuery>()),
        ).doReturn(listOf(exampleDimension))

        mockMvc
            .perform(
                post(SEARCH_VIEWABLE_DIMENSIONS_PATH)
                    .contentType("application/json")
                    .content(
                        """
                        {
                            "companyIds": ["test-company-id"],
                            "frameworksOrDataPointTypes": ["sfdr"],
                            "reportingPeriods": []
                        }
                        """.trimIndent(),
                    ).with(securityContext(mockSecurityContext)),
            ).andExpect(status().isOk)
    }

    @Test
    fun `searchViewableDimensions returns 401 for unauthenticated request`() {
        mockMvc
            .perform(
                post(SEARCH_VIEWABLE_DIMENSIONS_PATH)
                    .contentType("application/json")
                    .content(
                        """
                        {
                            "companyIds": ["test-company-id"],
                            "frameworksOrDataPointTypes": ["sfdr"],
                            "reportingPeriods": []
                        }
                        """.trimIndent(),
                    ),
            ).andExpect(status().isUnauthorized)
    }
}
