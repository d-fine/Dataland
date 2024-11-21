package org.dataland.datalandcommunitymanager.services

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandcommunitymanager.services.messaging.CompanyOwnershipAcceptedEmailMessageSender
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.email.CompanyOwnershipClaimApproved
import org.dataland.datalandmessagequeueutils.messages.email.EmailMessage
import org.dataland.datalandmessagequeueutils.messages.email.EmailRecipient
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.util.UUID

class CompanyOwnershipAcceptedEmailMessageSenderTest {
    private val objectMapper = jacksonObjectMapper()
    private lateinit var authenticationMock: DatalandJwtAuthentication
    private val cloudEventMessageHandlerMock = mock(CloudEventMessageHandler::class.java)
    private val companyName = "Test Inc."
    private val correlationId = UUID.randomUUID().toString()
    private val companyId = "59f05156-e1ba-4ea8-9d1e-d4833f6c7afc"
    private val userId = "1234-221-1111elf"
    private val numberOfOpenDataRequestsForCompany = 0

    @BeforeEach
    fun setupAuthentication() {
        val mockSecurityContext = mock(SecurityContext::class.java)
        authenticationMock =
            AuthenticationMock.mockJwtAuthentication(
                "userEmail",
                userId,
                setOf(DatalandRealmRole.ROLE_USER),
            )
        Mockito.`when`(mockSecurityContext.authentication).thenReturn(authenticationMock)
        Mockito.`when`(authenticationMock.credentials).thenReturn("")
        SecurityContextHolder.setContext(mockSecurityContext)
    }

    @Test
    fun `validate that the output of the external email message sender is correctly build for all frameworks`() {
        mockCloudEventMessageHandlerAndSetChecks()

        val dataRequestQueryManager = mock(DataRequestQueryManager::class.java)
        val companyOwnershipAcceptedEmailMessageSender =
            CompanyOwnershipAcceptedEmailMessageSender(
                cloudEventMessageHandlerMock,
                objectMapper,
                dataRequestQueryManager,
            )

        companyOwnershipAcceptedEmailMessageSender
            .sendCompanyOwnershipAcceptanceExternalEmailMessage(
                newCompanyOwnerId = userId,
                datalandCompanyId = companyId,
                companyName = companyName,
                correlationId = correlationId,
            )
    }

    private fun mockCloudEventMessageHandlerAndSetChecks() {
        Mockito
            .`when`(
                cloudEventMessageHandlerMock.buildCEMessageAndSendToQueue(
                    ArgumentMatchers.anyString(),
                    ArgumentMatchers.anyString(),
                    ArgumentMatchers.anyString(),
                    ArgumentMatchers.anyString(),
                    ArgumentMatchers.anyString(),
                ),
            ).then {
                val emailMessage = objectMapper.readValue(it.getArgument<String>(0), EmailMessage::class.java)
                val arg2 = it.getArgument<String>(1)
                val arg3 = it.getArgument<String>(2)
                val arg4 = it.getArgument<String>(3)
                val arg5 = it.getArgument<String>(4)
                Assertions.assertTrue(emailMessage.typedEmailContent is CompanyOwnershipClaimApproved)
                val companyOwnershipClaimApproved = emailMessage.typedEmailContent as CompanyOwnershipClaimApproved
                Assertions.assertEquals(companyId, companyOwnershipClaimApproved.companyId)
                Assertions.assertEquals(companyName, companyOwnershipClaimApproved.companyName)
                Assertions.assertEquals(
                    numberOfOpenDataRequestsForCompany, companyOwnershipClaimApproved.numberOfOpenDataRequestsForCompany,
                )
                Assertions.assertEquals(listOf(EmailRecipient.UserId(userId)), emailMessage.receiver)
                Assertions.assertEquals(MessageType.SEND_EMAIL, arg2)
                Assertions.assertEquals(correlationId, arg3)
                Assertions.assertEquals(ExchangeName.SEND_EMAIL, arg4)
                Assertions.assertEquals(RoutingKeyNames.EMAIL, arg5)
            }
    }
}
