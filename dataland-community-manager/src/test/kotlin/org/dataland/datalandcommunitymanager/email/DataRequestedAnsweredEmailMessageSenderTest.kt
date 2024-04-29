package org.dataland.datalandcommunitymanager.email

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandcommunitymanager.services.KeycloakUserControllerApiService
import org.dataland.datalandcommunitymanager.services.messaging.DataRequestedAnsweredEmailMessageSender
import org.dataland.datalandcommunitymanager.utils.RequestResonseEmailSenderUtils
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.TemplateEmailMessage
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.reset
import org.mockito.Mockito.`when`
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.util.*

class DataRequestedAnsweredEmailMessageSenderTest {
    private val requestResponseEmailSenderUtils = RequestResonseEmailSenderUtils()
    private val objectMapper = jacksonObjectMapper()
    private lateinit var dataRequestId: String
    private val cloudEventMessageHandlerMock = mock(CloudEventMessageHandler::class.java)
    private val keycloakUserControllerApiService = mock(KeycloakUserControllerApiService::class.java)
    private val correlationId = UUID.randomUUID().toString()
    private val userId = "1234-221-1111elf"
    private val userEmail = "$userId@example.com"
    private val staleDaysThreshold = "some Number"
    private val dataTypes = requestResponseEmailSenderUtils.getListOfAllDataTypes()

    @BeforeEach
    fun setupAuthentication() {
        val mockSecurityContext = mock(SecurityContext::class.java)
        val authenticationMock = AuthenticationMock.mockJwtAuthentication(
            userEmail,
            userId,
            setOf(DatalandRealmRole.ROLE_USER),
        )
        `when`(mockSecurityContext.authentication).thenReturn(authenticationMock)
        `when`(authenticationMock.credentials).thenReturn("")
        `when`(keycloakUserControllerApiService.getEmailAddress(userId)).thenReturn(userEmail)
        SecurityContextHolder.setContext(mockSecurityContext)
    }

    @Test
    fun `validate that the output of the external email message sender is correctly build for all frameworks`() {
        dataTypes.forEach {
            mockCloudEventMessageHandlerAndSetChecks(it[0], it[1])
            val dataRequestedAnsweredEmailMessageSender =
                DataRequestedAnsweredEmailMessageSender(
                    cloudEventMessageHandlerMock,
                    objectMapper, keycloakUserControllerApiService,
                    requestResponseEmailSenderUtils.getCompanyDataControllerMock(), staleDaysThreshold,
                )
            val dataRequestEntity = requestResponseEmailSenderUtils.getDataRequestEntityWithDataType(it[0])
            dataRequestId = dataRequestEntity.dataRequestId
            dataRequestedAnsweredEmailMessageSender
                .sendDataRequestedAnsweredEmail(dataRequestEntity, correlationId)
            reset(cloudEventMessageHandlerMock)
        }
    }
    private fun mockCloudEventMessageHandlerAndSetChecks(dataType: String, dataTypeDescription: String) {
        `when`(
            cloudEventMessageHandlerMock.buildCEMessageAndSendToQueue(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
            ),
        ).then() {
            val arg1 =
                objectMapper.readValue(it.getArgument<String>(0), TemplateEmailMessage::class.java)
            val arg2 = it.getArgument<String>(1)
            val arg3 = it.getArgument<String>(2)
            val arg4 = it.getArgument<String>(3)
            val arg5 = it.getArgument<String>(4)
            assertEquals(TemplateEmailMessage.Type.DataRequestedAnswered, arg1.emailTemplateType)
            assertEquals(userEmail, arg1.receiver)
            requestResponseEmailSenderUtils.checkPropertiesOfDataRequestResponseEmail(
                dataRequestId, arg1.properties, dataType, dataTypeDescription, staleDaysThreshold,
            )
            assertEquals(MessageType.SendTemplateEmail, arg2)
            assertEquals(correlationId, arg3)
            assertEquals(ExchangeName.SendEmail, arg4)
            assertEquals(RoutingKeyNames.templateEmail, arg5)
        }
    }
}
