@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.dataland.datalanduserservice.controller

import org.dataland.datalandbackendutils.model.KeycloakUserInfo
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalanduserservice.model.BasePortfolioName
import org.dataland.datalanduserservice.model.PortfolioUpload
import org.dataland.datalanduserservice.model.SupportRequestData
import org.dataland.datalanduserservice.service.MessageQueuePublisherService
import org.dataland.datalanduserservice.service.PortfolioEnrichmentService
import org.dataland.datalanduserservice.service.PortfolioMonitoringService
import org.dataland.datalanduserservice.service.PortfolioService
import org.dataland.datalanduserservice.utils.Validator
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.Mockito.reset
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PortfolioControllerTest {
    private val mockPortfolioService = mock<PortfolioService>()
    private val mockValidator = mock<Validator>()
    private val mockSecurityContext = mock<SecurityContext>()
    private val mockPortfolioEnrichmentService = mock<PortfolioEnrichmentService>()
    private val mockPortfolioMonitoringService = mock<PortfolioMonitoringService>()
    private lateinit var mockAuthentication: DatalandAuthentication
    private lateinit var portfolioController: PortfolioController

    private val mockKeycloakUserService = mock<KeycloakUserService>()
    private val mockCloudEventMessageHandler = mock<CloudEventMessageHandler>()
    private val publisher = MessageQueuePublisherService(mockCloudEventMessageHandler, mockKeycloakUserService)

    private val username = "data_reader"
    private val userId = "user-id"
    private val dummyPortfolioId = UUID.randomUUID()
    private val dummyPortfolioName = "Test Portfolio"
    private val validCompanyId = "valid-company-id"
    private val isMonitored = true
    private val dummyStartingMonitoringPeriod = "2023"
    private val dummyMonitoredFrameworks = mutableSetOf("sfdr", "eutaxonomy")

    private val validPortfolioUpload =
        PortfolioUpload(
            dummyPortfolioName,
            setOf(validCompanyId),
            isMonitored,
            dummyStartingMonitoringPeriod,
            dummyMonitoredFrameworks,
        )

    @BeforeEach
    fun setup() {
        reset(mockPortfolioService, mockValidator, mockPortfolioEnrichmentService, mockPortfolioMonitoringService)
        this.resetSecurityContext()
        doNothing().whenever(mockValidator).validatePortfolioCreation(eq(validPortfolioUpload), any())

        portfolioController =
            PortfolioController(
                mockPortfolioService,
                mockValidator,
                mockPortfolioEnrichmentService,
                mockPortfolioMonitoringService,
                publisher,
            )
    }

    /**
     * Setting the security context to use Data-land dummy user with role ROLE_USER
     */
    private fun resetSecurityContext() {
        mockAuthentication =
            AuthenticationMock.mockJwtAuthentication(username, userId, roles = setOf(DatalandRealmRole.ROLE_USER))
        doReturn(mockAuthentication).whenever(mockSecurityContext).authentication
        SecurityContextHolder.setContext(mockSecurityContext)
    }

    @Test
    fun `test that creating a valid portfolio returns 201 response`() {
        doReturn(false).whenever(mockPortfolioService).existsPortfolioWithNameForUser(eq(dummyPortfolioName), any())
        val response = assertDoesNotThrow { portfolioController.createPortfolio(validPortfolioUpload) }
        assertEquals(HttpStatus.CREATED, response.statusCode)
    }

    @Test
    fun `test that replacing an existing portfolio by a valid portfolio returns 200 response`() {
        doReturn(true).whenever(mockPortfolioService).existsPortfolioForUser(eq(dummyPortfolioId.toString()), any())

        val response =
            assertDoesNotThrow {
                portfolioController.replacePortfolio(
                    dummyPortfolioId.toString(),
                    validPortfolioUpload,
                )
            }
        assertEquals(HttpStatus.OK, response.statusCode)
    }

    @Test
    fun `test that retrieving portfolio names for all portfolios of a user works as expected`() {
        val portfolios = listOf(BasePortfolioName("12345", "pension"), BasePortfolioName("0815", "small-cap"))
        doReturn(portfolios).whenever(mockPortfolioService).getAllPortfolioNamesForCurrentUser()
        val response = assertDoesNotThrow { portfolioController.getAllPortfolioNamesForCurrentUser() }.body
        assertEquals(portfolios, response)
    }

    @Test
    fun `verify support request is published`() {
        val supportRequestData = SupportRequestData(topic = "test topic", message = "test message")
        whenever(mockKeycloakUserService.getUser(any())).thenReturn(
            KeycloakUserInfo(
                userId = "test userId",
                email = "test email",
                firstName = "test firstName",
                lastName = "test lastName",
            ),
        )
        portfolioController.postSupportRequest(supportRequestData)

        verify(mockCloudEventMessageHandler, times(1)).buildCEMessageAndSendToQueue(
            body = any(),
            type = any(),
            correlationId = any(),
            exchange = any(),
            routingKey = any(),
        )
    }
}
