package org.dataland.datalanduserservice.service

import org.dataland.datalandbackendutils.model.KeycloakUserInfo
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.data.PortfolioUpdatePayload
import org.dataland.datalandmessagequeueutils.messages.email.EmailMessage
import org.dataland.datalandmessagequeueutils.messages.email.EmailRecipient
import org.dataland.datalandmessagequeueutils.messages.email.InternalEmailContentTable
import org.dataland.datalandmessagequeueutils.messages.email.Value
import org.dataland.datalanduserservice.model.SupportRequestData
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder

class MessageQueuePublisherServiceTest {
    private val mockCloudEventMessageHandler = mock<CloudEventMessageHandler>()
    private val mockKeycloakUserService = mock<KeycloakUserService>()

    private lateinit var messageQueuePublisherService: MessageQueuePublisherService
    private val mockSecurityContext = mock<SecurityContext>()

    private val testUserId = "test-user-id"
    private val userRoles = setOf(DatalandRealmRole.ROLE_USER, DatalandRealmRole.ROLE_UPLOADER)
    private val testCorrelationId = "test-correlation-id"
    private val objectMapper = defaultObjectMapper

    private fun resetSecurityContext() {
        val mockAuthentication =
            AuthenticationMock.mockJwtAuthentication(
                "username",
                testUserId,
                userRoles,
            )
        doReturn(mockAuthentication).whenever(mockSecurityContext).authentication
        SecurityContextHolder.setContext(mockSecurityContext)
    }

    @BeforeEach
    fun setup() {
        reset(mockCloudEventMessageHandler, mockKeycloakUserService)
        messageQueuePublisherService =
            MessageQueuePublisherService(
                cloudEventMessageHandler = mockCloudEventMessageHandler,
                keycloakUserService = mockKeycloakUserService,
            )
        resetSecurityContext()
    }

    @Test
    fun `test publishPortfolioUpdate sends correct payload to message handler`() {
        val portfolioId = "portfolio-123"
        val companyIds = setOf("company-1", "company-2")
        val monitoredFrameworks = setOf("framework-1", "framework-2")
        val reportingPeriods = setOf("2022-Q1", "2022-Q2")
        val userRolesAsList = mutableListOf<String>()
        userRoles.forEach { userRolesAsList.add(it.toString()) }

        val portfolioPayload =
            PortfolioUpdatePayload(
                portfolioId = portfolioId,
                companyIds = companyIds,
                monitoredFrameworks = monitoredFrameworks,
                reportingPeriods = reportingPeriods,
                userId = testUserId,
                userRoles = userRolesAsList,
            )

        messageQueuePublisherService.publishPortfolioUpdate(
            portfolioId = portfolioId,
            companyIds = companyIds,
            monitoredFrameworks = monitoredFrameworks,
            reportingPeriods = reportingPeriods,
            correlationId = testCorrelationId,
        )

        verify(mockCloudEventMessageHandler, times(1)).buildCEMessageAndSendToQueue(
            objectMapper.writeValueAsString(portfolioPayload),
            MessageType.PORTFOLIO_UPDATE,
            testCorrelationId,
            ExchangeName.USER_SERVICE_EVENTS,
            RoutingKeyNames.PORTFOLIO_UPDATE,
        )
    }

    @Test
    fun `test publishSupportRequest sends correct email details`() {
        val supportRequestData =
            SupportRequestData(
                topic = "Test Topic",
                message = "Test Message",
            )
        val mockUser =
            KeycloakUserInfo(
                userId = testUserId,
                email = "testuser@dataland.com",
                firstName = "Test",
                lastName = "User",
            )

        val internalEmailContentTable =
            InternalEmailContentTable(
                "User Portfolio Support Request",
                "A user has submitted a request for support.",
                listOf(
                    "User" to Value.Text(mockUser.userId),
                    "E-Mail" to Value.Text(mockUser.email!!),
                    "First Name" to Value.Text(mockUser.firstName!!),
                    "Last Name" to Value.Text(mockUser.lastName!!),
                    "Topic" to Value.Text(supportRequestData.topic),
                    "Message" to Value.Text(supportRequestData.message),
                ),
            )
        val message =
            EmailMessage(
                internalEmailContentTable,
                listOf(EmailRecipient.Internal),
                listOf(EmailRecipient.InternalCc),
                emptyList(),
            )

        whenever(mockKeycloakUserService.getUser(testUserId)).thenReturn(mockUser)

        messageQueuePublisherService.publishSupportRequest(
            supportRequestData = supportRequestData,
            correlationId = testCorrelationId,
        )

        verify(mockCloudEventMessageHandler, times(1)).buildCEMessageAndSendToQueue(
            objectMapper.writeValueAsString(message),
            MessageType.SEND_EMAIL,
            testCorrelationId,
            ExchangeName.SEND_EMAIL,
            RoutingKeyNames.EMAIL,
        )
    }
}
