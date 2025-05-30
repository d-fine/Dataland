package org.dataland.datalandcommunitymanager.email

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.services.messaging.DataRequestResponseEmailBuilder
import org.dataland.datalandcommunitymanager.utils.CompanyInfoService
import org.dataland.datalandcommunitymanager.utils.TestUtils
import org.dataland.datalandcommunitymanager.utils.readableFrameworkNameMapping
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.email.DataAvailableEmailContent
import org.dataland.datalandmessagequeueutils.messages.email.EmailMessage
import org.dataland.datalandmessagequeueutils.messages.email.EmailRecipient
import org.dataland.datalandmessagequeueutils.messages.email.TypedEmailContent
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
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
        TestUtils.mockSecurityContext("userEmail", userId, DatalandRealmRole.ROLE_USER)
    }

    private fun getDataRequestEntityWithDataType(dataType: String): DataRequestEntity =
        DataRequestEntity(
            userId = userId,
            dataType = dataType,
            notifyMeImmediately = false,
            reportingPeriod = reportingPeriod,
            datalandCompanyId = companyId,
            creationTimestamp = creationTimestamp,
        )

    private fun getCompanyInfoServiceMock(): CompanyInfoService {
        val companyInfoServiceMock = mock(CompanyInfoService::class.java)
        `when`(companyInfoServiceMock.getValidCompanyNameOrId(companyId))
            .thenReturn(companyName)
        return companyInfoServiceMock
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
            assertTrue(emailData is DataAvailableEmailContent)
            val dataAvailableEmailContent = emailData as DataAvailableEmailContent
            assertEquals(companyName, dataAvailableEmailContent.companyName)
            assertEquals(dataTypeLabel, dataAvailableEmailContent.dataTypeLabel)
            assertEquals(reportingPeriod, dataAvailableEmailContent.reportingPeriod)
            assertEquals(creationTimestampAsDate, dataAvailableEmailContent.creationDate)
            assertEquals(dataRequestId, dataAvailableEmailContent.dataRequestId)
            assertEquals(staleDaysThreshold, dataAvailableEmailContent.closedInDays)
        }

    @Test
    fun `check that the output of the answered request email message sender is correctly built for all frameworks`() {
        dataTypes.forEach {
            val dataRequestEntity = getDataRequestEntityWithDataType(it.key)
            val dataRequestId = dataRequestEntity.dataRequestId
            val cloudEventMessageHandlerMock =
                getMockCloudEventMessageHandlerAndSetChecks(
                    assertAnsweredEmailData(dataRequestId, it.value),
                )

            val dataRequestClosedEmailMessageSender =
                DataRequestResponseEmailBuilder(
                    cloudEventMessageHandlerMock,
                    getCompanyInfoServiceMock(),
                    objectMapper,
                    staleDaysThreshold.toString(),
                )
            dataRequestClosedEmailMessageSender.buildDataRequestAnsweredEmailAndSendCEMessage(
                dataRequestEntity, correlationId,
            )
        }
    }
}
