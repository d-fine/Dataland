package org.dataland.datalanduserservice.service

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalanduserservice.DatalandUserService
import org.dataland.datalanduserservice.entity.PortfolioEntity
import org.dataland.datalanduserservice.model.enums.NotificationFrequency
import org.dataland.datalanduserservice.repository.PortfolioRepository
import org.dataland.datalanduserservice.utils.PortfolioRightsUtilsComponent
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
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
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@SpringBootTest(
    classes = [DatalandUserService::class],
    properties = ["spring.profiles.active=nodb"],
)
@AutoConfigureMockMvc
class PortfolioControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val portfolioRepository: PortfolioRepository,
) {
    @MockitoBean(name = "PortfolioRightsUtilsComponent")
    private lateinit var portfolioRightsUtilsComponent: PortfolioRightsUtilsComponent

    @MockitoBean
    lateinit var companyDataController: CompanyDataControllerApi

    private val mockSecurityContext = mock<SecurityContext>()

    private val adminUserId = UUID.randomUUID()
    private val regularUserId = UUID.randomUUID()

    private val monitoredRequestBody =
        """
        {
          "portfolioName": "Monitored Portfolio",
          "identifiers": ["company-1"],
          "isMonitored": true,
          "monitoredFrameworks": ["sfdr"],
          "notificationFrequency": "Weekly",
          "timeWindowThreshold": "Standard",
          "sharedUserIds": []
        }
        """.trimIndent()
    private val notMonitoredRequestBody =
        """
        {
          "portfolioName": "Unmonitored Portfolio",
          "identifiers": ["company-1"],
          "isMonitored": false,
          "monitoredFrameworks": [],
          "notificationFrequency": "Weekly",
          "timeWindowThreshold": null,
          "sharedUserIds": []
        }
        """.trimIndent()

        private val monitoredPortfolioPatchRequestBody =  """
            {
              "isMonitored": true, 
              "monitoredFrameworks": ["sfdr"], 
              "notificationFrequency": "Weekly", 
              "timeWindowThreshold": "Standard"
            }
            """.trimIndent()


    private val dummyAdminAuthentication: DatalandJwtAuthentication =
        AuthenticationMock.mockJwtAuthentication(
            username = "PORTFOLIO_ADMIN",
            userId = adminUserId.toString(),
            roles = setOf(DatalandRealmRole.ROLE_ADMIN, DatalandRealmRole.ROLE_USER),
        )

    private val dummyUserAuthentication: DatalandJwtAuthentication =
        AuthenticationMock.mockJwtAuthentication(
            username = "PORTFOLIO_USER",
            userId = regularUserId.toString(),
            roles = setOf(DatalandRealmRole.ROLE_USER),
        )

    private val portfolioId = UUID.randomUUID().toString()

    @BeforeEach
    fun setup() {
        reset(
            mockSecurityContext,
            portfolioRightsUtilsComponent,
            companyDataController,
        )
        val entity =
            PortfolioEntity(
                portfolioId = UUID.fromString(portfolioId),
                userId = regularUserId.toString(),
                portfolioName = "Test Portfolio",
                creationTimestamp = System.currentTimeMillis(),
                lastUpdateTimestamp = System.currentTimeMillis(),
                companyIds = mutableSetOf("company-1"),
                isMonitored = false,
                monitoredFrameworks = emptySet(),
                notificationFrequency = NotificationFrequency.Weekly,
                timeWindowThreshold = null,
                sharedUserIds = emptySet(),
            )
        portfolioRepository.save(entity)
    }

    private fun setMockSecurityContext(authentication: DatalandJwtAuthentication) {
        doReturn(authentication).whenever(mockSecurityContext).authentication
        SecurityContextHolder.setContext(mockSecurityContext)
    }

    private fun performGetPortfolioAndExpect(statusMatcher: org.springframework.test.web.servlet.ResultMatcher) {
        mockMvc
            .perform(
                get("/portfolios/$portfolioId/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(securityContext(mockSecurityContext)),
            ).andExpect(statusMatcher)
    }

    private fun performCreatePortfolioAndExpect(
        requestBody: String,
        statusMatcher: org.springframework.test.web.servlet.ResultMatcher,
    ) {
        mockMvc
            .perform(
                post("/portfolios/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
                    .with(securityContext(mockSecurityContext)),
            ).andExpect(statusMatcher)
    }

    private fun performReplacePortfolioAndExpect(
        requestBody: String,
        statusMatcher: org.springframework.test.web.servlet.ResultMatcher,
    ) {
        mockMvc
            .perform(
                put("/portfolios/$portfolioId/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
                    .with(securityContext(mockSecurityContext)),
            ).andExpect(statusMatcher)
    }

    private fun performPatchMonitoringAndExpect(
        requestBody: String,
        statusMatcher: org.springframework.test.web.servlet.ResultMatcher,
    ) {
        mockMvc
            .perform(
                patch("/portfolios/$portfolioId/monitoring")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
                    .with(securityContext(mockSecurityContext)),
            ).andExpect(statusMatcher)
    }

    // -----------------------
    // getPortfolio security
    // -----------------------

    @Test
    fun `admins can get any portfolio`() {
        setMockSecurityContext(dummyAdminAuthentication)
        performGetPortfolioAndExpect(status().isOk)
    }

    @Test
    fun `regular users cannot get portfolio if they are neither owner nor portfolio is shared`() {
        setMockSecurityContext(dummyUserAuthentication)

        whenever(portfolioRightsUtilsComponent.isUserPortfolioOwner(portfolioId)).thenReturn(false)
        whenever(
            portfolioRightsUtilsComponent.isPortfolioSharedWithUser(
                any(), // userId from authentication.userId
                org.mockito.kotlin.eq(portfolioId),
            ),
        ).thenReturn(false)

        performGetPortfolioAndExpect(status().isForbidden)
    }

    @Test
    fun `regular users can get portfolio if they are owner`() {
        setMockSecurityContext(dummyUserAuthentication)

        whenever(portfolioRightsUtilsComponent.isUserPortfolioOwner(portfolioId)).thenReturn(true)

        whenever(
            portfolioRightsUtilsComponent.isPortfolioSharedWithUser(
                any(),
                org.mockito.kotlin.eq(portfolioId),
            ),
        ).thenReturn(false)

        performGetPortfolioAndExpect(status().isOk)
    }

    @Test
    fun `regular users can get portfolio if portfolio is shared with them`() {
        setMockSecurityContext(dummyUserAuthentication)

        whenever(portfolioRightsUtilsComponent.isUserPortfolioOwner(portfolioId)).thenReturn(false)
        whenever(
            portfolioRightsUtilsComponent.isPortfolioSharedWithUser(
                any(),
                org.mockito.kotlin.eq(portfolioId),
            ),
        ).thenReturn(true)

        performGetPortfolioAndExpect(status().isOk)
    }

    // -----------------------
    // createPortfolio security
    // -----------------------

    @Test
    fun `admins can create monitored portfolios`() {
        setMockSecurityContext(dummyAdminAuthentication)

        doNothing().whenever(companyDataController).isCompanyIdValid(any())

        performCreatePortfolioAndExpect(monitoredRequestBody, status().isCreated)
    }

    @Test
    fun `regular users cannot create monitored portfolios if not allowed by rights component`() {
        setMockSecurityContext(dummyUserAuthentication)

        whenever(
            portfolioRightsUtilsComponent.mayNonAdminUserManipulatePortfolioMonitoring(
                any(), // authentication.userId
                org.mockito.kotlin.eq(true),
            ),
        ).thenReturn(false)

        doNothing().whenever(companyDataController).isCompanyIdValid(any())

        performCreatePortfolioAndExpect(monitoredRequestBody, status().isForbidden)
    }

    @Test
    fun `regular users can create monitored portfolios if allowed by rights component`() {
        setMockSecurityContext(dummyUserAuthentication)

        whenever(
            portfolioRightsUtilsComponent.mayNonAdminUserManipulatePortfolioMonitoring(
                any(),
                org.mockito.kotlin.eq(true),
            ),
        ).thenReturn(true)

        doNothing().whenever(companyDataController).isCompanyIdValid(any())

        performCreatePortfolioAndExpect(monitoredRequestBody, status().isCreated)
    }

    @Test
    fun `regular users can always create unmonitored portfolios`() {
        setMockSecurityContext(dummyUserAuthentication)

        whenever(
            portfolioRightsUtilsComponent.mayNonAdminUserManipulatePortfolioMonitoring(
                any(),
                org.mockito.kotlin.eq(false),
            ),
        ).thenReturn(true)

        doNothing().whenever(companyDataController).isCompanyIdValid(any())

        performCreatePortfolioAndExpect(notMonitoredRequestBody, status().isCreated)
    }

    // -----------------------
    // replacePortfolio security
    // -----------------------

    @Test
    fun `admins can replace any portfolio`() {
        setMockSecurityContext(dummyAdminAuthentication)
        doNothing().whenever(companyDataController).isCompanyIdValid(any())
        performReplacePortfolioAndExpect(monitoredRequestBody, status().isOk)
    }

    @Test
    fun `regular users cannot replace portfolio they do not own`() {
        setMockSecurityContext(dummyUserAuthentication)

        whenever(portfolioRightsUtilsComponent.isUserPortfolioOwner(portfolioId)).thenReturn(false)
        whenever(
            portfolioRightsUtilsComponent.mayNonAdminUserManipulatePortfolioMonitoring(
                any(),
                org.mockito.kotlin.eq(true),
            ),
        ).thenReturn(true)
        doNothing().whenever(companyDataController).isCompanyIdValid(any())

        performReplacePortfolioAndExpect(monitoredRequestBody, status().isForbidden)
    }

    @Test
    fun `regular users can replace their own portfolio if monitoring manipulation is allowed`() {
        setMockSecurityContext(dummyUserAuthentication)

        whenever(portfolioRightsUtilsComponent.isUserPortfolioOwner(portfolioId)).thenReturn(true)
        whenever(
            portfolioRightsUtilsComponent.mayNonAdminUserManipulatePortfolioMonitoring(
                any(),
                org.mockito.kotlin.eq(true),
            ),
        ).thenReturn(true)

        doNothing().whenever(companyDataController).isCompanyIdValid(any())

        performReplacePortfolioAndExpect(monitoredRequestBody, status().isOk)
    }

    @Test
    fun `regular users cannot replace their own portfolio with monitored flag if not allowed`() {
        setMockSecurityContext(dummyUserAuthentication)

        whenever(portfolioRightsUtilsComponent.isUserPortfolioOwner(portfolioId)).thenReturn(true)
        whenever(
            portfolioRightsUtilsComponent.mayNonAdminUserManipulatePortfolioMonitoring(
                any(),
                org.mockito.kotlin.eq(true),
            ),
        ).thenReturn(false)

        doNothing().whenever(companyDataController).isCompanyIdValid(any())
        performReplacePortfolioAndExpect(monitoredRequestBody, status().isForbidden)
    }

    // -----------------------
    // patchMonitoring security
    // -----------------------

    @Test
    fun `admins can patch monitoring of any portfolio`() {
        setMockSecurityContext(dummyAdminAuthentication)

        performPatchMonitoringAndExpect(monitoredPortfolioPatchRequestBody, status().isOk)
    }

    @Test
    fun `regular users cannot patch monitoring if they are not the owner`() {
        setMockSecurityContext(dummyUserAuthentication)

        whenever(portfolioRightsUtilsComponent.isUserPortfolioOwner(portfolioId)).thenReturn(false)
        whenever(
            portfolioRightsUtilsComponent.mayNonAdminUserManipulatePortfolioMonitoring(
                any(),
                org.mockito.kotlin.eq(true),
            ),
        ).thenReturn(true)

        performPatchMonitoringAndExpect(monitoredPortfolioPatchRequestBody, status().isForbidden)
    }

    @Test
    fun `regular users can patch monitoring if they are owner and rights component allows it`() {
        setMockSecurityContext(dummyUserAuthentication)

        whenever(portfolioRightsUtilsComponent.isUserPortfolioOwner(portfolioId)).thenReturn(true)
        whenever(
            portfolioRightsUtilsComponent.mayNonAdminUserManipulatePortfolioMonitoring(
                any(),
                org.mockito.kotlin.eq(true),
            ),
        ).thenReturn(true)

        performPatchMonitoringAndExpect(monitoredPortfolioPatchRequestBody, status().isOk)
    }

    @Test
    fun `regular users cannot enable monitoring if rights component forbids it even when owner`() {
        setMockSecurityContext(dummyUserAuthentication)

        whenever(portfolioRightsUtilsComponent.isUserPortfolioOwner(portfolioId)).thenReturn(true)
        whenever(
            portfolioRightsUtilsComponent.mayNonAdminUserManipulatePortfolioMonitoring(
                any(),
                org.mockito.kotlin.eq(true),
            ),
        ).thenReturn(false)

        performPatchMonitoringAndExpect(monitoredPortfolioPatchRequestBody, status().isForbidden)
    }
}
