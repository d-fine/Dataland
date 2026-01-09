package org.dataland.datalanduserservice.controller

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.datalanduserservice.DatalandUserService
import org.dataland.datalanduserservice.entity.PortfolioEntity
import org.dataland.datalanduserservice.model.PortfolioMonitoringPatch
import org.dataland.datalanduserservice.model.PortfolioSharingPatch
import org.dataland.datalanduserservice.model.PortfolioUpload
import org.dataland.datalanduserservice.model.TimeWindowThreshold
import org.dataland.datalanduserservice.model.enums.NotificationFrequency
import org.dataland.datalanduserservice.repository.PortfolioRepository
import org.dataland.datalanduserservice.utils.PortfolioRightsUtilsComponent
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import java.util.UUID

@SpringBootTest(
    classes = [DatalandUserService::class],
    properties = ["spring.profiles.active=nodb"],
)
@AutoConfigureMockMvc
abstract class AbstractPortfolioControllerTest {
    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var portfolioRepository: PortfolioRepository

    @MockitoBean
    protected lateinit var portfolioRightsUtilsComponent: PortfolioRightsUtilsComponent

    @MockitoBean
    protected lateinit var companyDataController: CompanyDataControllerApi

    protected val mockSecurityContext: SecurityContext = mock()

    protected val adminUserId: UUID = UUID.randomUUID()
    protected val regularUserId: UUID = UUID.randomUUID()
    protected val sharedUserId: UUID = UUID.randomUUID()
    protected val dummyCompanyId = "dummyCompanyId"

    protected val monitoredRequestBody: String =
        defaultObjectMapper.writeValueAsString(
            PortfolioUpload(
                portfolioName = "Monitored Portfolio",
                identifiers = setOf(dummyCompanyId),
                isMonitored = true,
                monitoredFrameworks = setOf("sfdr"),
                notificationFrequency = NotificationFrequency.Weekly,
                timeWindowThreshold = TimeWindowThreshold.Standard,
                sharedUserIds = emptySet(),
            ),
        )

    protected val notMonitoredRequestBody: String =
        defaultObjectMapper.writeValueAsString(
            PortfolioUpload(
                portfolioName = "Unmonitored Portfolio",
                identifiers = setOf(dummyCompanyId),
                isMonitored = false,
                monitoredFrameworks = emptySet(),
                notificationFrequency = NotificationFrequency.Weekly,
                timeWindowThreshold = null,
                sharedUserIds = emptySet(),
            ),
        )

    protected val monitoredPortfolioPatchRequestBody: String =
        defaultObjectMapper.writeValueAsString(
            PortfolioMonitoringPatch(
                isMonitored = true,
                monitoredFrameworks = setOf("sfdr"),
                notificationFrequency = NotificationFrequency.Weekly,
                timeWindowThreshold = TimeWindowThreshold.Standard,
            ),
        )

    protected val sharingPatchRequestBody: String =
        defaultObjectMapper.writeValueAsString(
            PortfolioSharingPatch(
                sharedUserIds = setOf(sharedUserId.toString()),
            ),
        )

    protected val dummyAdminAuthentication: DatalandJwtAuthentication =
        AuthenticationMock.mockJwtAuthentication(
            username = "PORTFOLIO_ADMIN",
            userId = adminUserId.toString(),
            roles = setOf(DatalandRealmRole.ROLE_ADMIN, DatalandRealmRole.ROLE_USER),
        )

    protected val dummyUserAuthentication: DatalandJwtAuthentication =
        AuthenticationMock.mockJwtAuthentication(
            username = "PORTFOLIO_USER",
            userId = regularUserId.toString(),
            roles = setOf(DatalandRealmRole.ROLE_USER),
        )

    protected val portfolioId: String = UUID.randomUUID().toString()

    @BeforeEach
    fun setupBase() {
        reset(
            mockSecurityContext,
            portfolioRightsUtilsComponent,
            companyDataController,
        )
        portfolioRepository.deleteAll()

        val entity =
            PortfolioEntity(
                portfolioId = UUID.fromString(portfolioId),
                userId = regularUserId.toString(),
                portfolioName = "Test Portfolio",
                creationTimestamp = System.currentTimeMillis(),
                lastUpdateTimestamp = System.currentTimeMillis(),
                companyIds = mutableSetOf(dummyCompanyId),
                isMonitored = false,
                monitoredFrameworks = emptySet(),
                notificationFrequency = NotificationFrequency.Weekly,
                timeWindowThreshold = null,
                sharedUserIds = emptySet(),
            )
        portfolioRepository.save(entity)
    }

    protected fun setMockSecurityContext(authentication: DatalandJwtAuthentication) {
        doReturn(authentication).whenever(mockSecurityContext).authentication
        SecurityContextHolder.setContext(mockSecurityContext)
    }

    protected fun performGetPortfolioAndExpect(statusMatcher: ResultMatcher) {
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .get("/portfolios/$portfolioId/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(SecurityMockMvcRequestPostProcessors.securityContext(mockSecurityContext)),
            ).andExpect(statusMatcher)
    }

    protected fun performCreatePortfolioAndExpect(
        requestBody: String,
        statusMatcher: ResultMatcher,
    ) {
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/portfolios/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
                    .with(SecurityMockMvcRequestPostProcessors.securityContext(mockSecurityContext)),
            ).andExpect(statusMatcher)
    }

    protected fun performReplacePortfolioAndExpect(
        requestBody: String,
        statusMatcher: ResultMatcher,
    ) {
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .put("/portfolios/$portfolioId/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
                    .with(SecurityMockMvcRequestPostProcessors.securityContext(mockSecurityContext)),
            ).andExpect(statusMatcher)
    }

    protected fun performPatchMonitoringAndExpect(
        requestBody: String,
        statusMatcher: ResultMatcher,
    ) {
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .patch("/portfolios/$portfolioId/monitoring")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
                    .with(SecurityMockMvcRequestPostProcessors.securityContext(mockSecurityContext)),
            ).andExpect(statusMatcher)
    }

    protected fun performGetSharedPortfoliosAndExpect(statusMatcher: ResultMatcher) {
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .get("/portfolios/shared")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(SecurityMockMvcRequestPostProcessors.securityContext(mockSecurityContext)),
            ).andExpect(statusMatcher)
    }

    protected fun performGetSharedPortfolioNamesAndExpect(statusMatcher: ResultMatcher) {
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .get("/portfolios/shared/names")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(SecurityMockMvcRequestPostProcessors.securityContext(mockSecurityContext)),
            ).andExpect(statusMatcher)
    }

    protected fun performPatchSharingAndExpect(
        requestBody: String,
        statusMatcher: ResultMatcher,
    ) {
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .patch("/portfolios/$portfolioId/sharing")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
                    .with(SecurityMockMvcRequestPostProcessors.securityContext(mockSecurityContext)),
            ).andExpect(statusMatcher)
    }

    protected fun performDeleteCurrentUserFromSharingAndExpect(statusMatcher: ResultMatcher) {
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .delete("/portfolios/shared/$portfolioId")
                    .with(SecurityMockMvcRequestPostProcessors.securityContext(mockSecurityContext)),
            ).andExpect(statusMatcher)
    }

    protected fun performDeletePortfolioAndExpect(statusMatcher: ResultMatcher) {
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .delete("/portfolios/$portfolioId/")
                    .with(SecurityMockMvcRequestPostProcessors.securityContext(mockSecurityContext)),
            ).andExpect(statusMatcher)
    }
}
