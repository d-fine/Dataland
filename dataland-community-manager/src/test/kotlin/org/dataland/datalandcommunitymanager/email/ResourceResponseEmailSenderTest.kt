package org.dataland.datalandcommunitymanager.email

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.services.messaging.DataRequestResponseEmailSender
import org.dataland.datalandcommunitymanager.utils.readableFrameworkNameMapping
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.email.DataRequestAnswered
import org.dataland.datalandmessagequeueutils.messages.email.EmailMessage
import org.dataland.datalandmessagequeueutils.messages.email.EmailRecipient
import org.dataland.datalandmessagequeueutils.messages.email.TypedEmailContent
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.util.UUID

class ResourceResponseEmailSenderTest {
    private val reportingPeriod = "2022"
    private val companyId = "59f05156-e1ba-4ea8-9d1e-d4833f6c7afc"
    private val userId = "1234-221-1111elf"
    private val creationTimestamp = 1709820187875
    private val creationTimestampAsDate = "07 Mar 2024, 15:03"
    private val companyName = "Test Inc."
    private val objectMapper = jacksonObjectMapper()
    private val correlationId = UUID.randomUUID().toString()
    private val staleDaysThreshold = 34
    private val dataTypes = readableFrameworkNameMapping.mapKeys { it.key.value }

    @BeforeEach
    fun setupAuthentication() {
        val mockSecurityContext = mock(SecurityContext::class.java)
        val authenticationMock =
            AuthenticationMock.mockJwtAuthentication(
                "userEmail",
                userId,
                setOf(DatalandRealmRole.ROLE_USER),
            )
        `when`(mockSecurityContext.authentication).thenReturn(authenticationMock)
        `when`(authenticationMock.credentials).thenReturn("")
        SecurityContextHolder.setContext(mockSecurityContext)
    }

    private fun getDataRequestEntityWithDataType(dataType: String): DataRequestEntity =
        DataRequestEntity(
            userId = userId,
            dataType = dataType,
            emailOnUpdate = false,
            reportingPeriod = reportingPeriod,
            datalandCompanyId = companyId,
            creationTimestamp = creationTimestamp,
        )

    private fun getCompanyDataControllerMock(): CompanyDataControllerApi {
        val companyDataControllerMock = mock(CompanyDataControllerApi::class.java)
        `when`(companyDataControllerMock.getCompanyInfo(companyId))
            .thenReturn(
                CompanyInformation(
                    companyName = companyName,
                    headquarters = "",
                    identifiers = emptyMap(),
                    countryCode = "",
                ),
            )
        return companyDataControllerMock
    }

    private fun getMockCloudEventMessageHandlerAndSetChecks(assertEmailData: (TypedEmailContent) -> Unit): CloudEventMessageHandler {
        val cloudEventMessageHandlerMock = mock(CloudEventMessageHandler::class.java)
        `when`(
            cloudEventMessageHandlerMock.buildCEMessageAndSendToQueue(
                any(), any(), any(), any(), any(),
            ),
        ).then {
            val emailMessage = objectMapper.readValue(it.getArgument<String>(0), EmailMessage::class.java)
            assertEquals(listOf(EmailRecipient.UserId(userId)), emailMessage.receiver)
            assertEmailData(emailMessage.typedEmailContent)
            assertEquals(MessageType.SEND_EMAIL, it.getArgument<String>(1))
            assertEquals(correlationId, it.getArgument<String>(2))
            assertEquals(ExchangeName.SEND_EMAIL, it.getArgument<String>(3))
            assertEquals(RoutingKeyNames.EMAIL, it.getArgument<String>(4))
        }
        return cloudEventMessageHandlerMock
    }

    private fun assertAnsweredEmailData(
        dataRequestId: String,
        dataTypeLabel: String,
    ): (TypedEmailContent) -> Unit =
        { emailData ->
            assertTrue(emailData is DataRequestAnswered)
            val dataRequestAnswered = emailData as DataRequestAnswered
            assertEquals(companyName, dataRequestAnswered.companyName)
            assertEquals(dataTypeLabel, dataRequestAnswered.dataTypeLabel)
            assertEquals(reportingPeriod, dataRequestAnswered.reportingPeriod)
            assertEquals(creationTimestampAsDate, dataRequestAnswered.creationDate)
            assertEquals(dataRequestId, dataRequestAnswered.dataRequestId)
            assertEquals(staleDaysThreshold, dataRequestAnswered.closedInDays)
        }

    @Test
    fun `check that the output of the answered request email message sender is correctly build for all frameworks`() {
        dataTypes.forEach {
            val dataRequestEntity = getDataRequestEntityWithDataType(it.key)
            val dataRequestId = dataRequestEntity.dataRequestId
            val cloudEventMessageHandlerMock =
                getMockCloudEventMessageHandlerAndSetChecks(
                    assertAnsweredEmailData(dataRequestId, it.value),
                )

            val dataRequestClosedEmailMessageSender =
                DataRequestResponseEmailSender(
                    cloudEventMessageHandlerMock,
                    objectMapper,
                    getCompanyDataControllerMock(),
                    staleDaysThreshold.toString(),
                )
            dataRequestClosedEmailMessageSender.sendDataRequestAnsweredEmail(
                dataRequestEntity, correlationId,
            )
        }
    }
}
