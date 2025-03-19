package org.dataland.datalandcommunitymanager.email

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.entities.ElementaryEventEntity
import org.dataland.datalandcommunitymanager.events.NotificationEventType
import org.dataland.datalandcommunitymanager.services.NotificationService
import org.dataland.datalandcommunitymanager.services.messaging.NotificationEmailSender
import org.dataland.datalandcommunitymanager.utils.readableFrameworkNameMapping
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.email.EmailMessage
import org.dataland.datalandmessagequeueutils.messages.email.EmailRecipient
import org.dataland.datalandmessagequeueutils.messages.email.InternalEmailContentTable
import org.dataland.datalandmessagequeueutils.messages.email.MultipleDatasetsUploadedEngagement
import org.dataland.datalandmessagequeueutils.messages.email.SingleDatasetUploadedEngagement
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import java.time.Instant
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NotificationEmailSenderTest {
    private val correlationId = UUID.randomUUID().toString()
    private val companyId = UUID.randomUUID()
    private val companyName = "testCompany"
    private val dataType = DataTypeEnum.sfdr
    private val reportingPeriod = "2020"
    private val receiver = "test@example.com"
    private val daysSinceLastNotificationEvent: Long = 30

    private lateinit var notificationEmailSender: NotificationEmailSender
    private lateinit var cloudEventMessageHandler: CloudEventMessageHandler

    @BeforeEach
    fun setup() {
        cloudEventMessageHandler = mock<CloudEventMessageHandler>()
        notificationEmailSender =
            NotificationEmailSender(
                cloudEventMessageHandler, jacksonObjectMapper(),
            )
    }

    private fun parseJsonStringIntoEmailMessage(jsonString: String): EmailMessage =
        notificationEmailSender.objectMapper.readValue(
            jsonString,
            EmailMessage::class.java,
        )

    private fun initElementaryEvent(
        dataType: DataTypeEnum,
        reportingPeriod: String,
    ): ElementaryEventEntity =
        ElementaryEventEntity(
            elementaryEventType = NotificationEventType.UploadEvent,
            companyId = companyId, framework = dataType,
            reportingPeriod = reportingPeriod, creationTimestamp = Instant.now().toEpochMilli(),
            notificationEvent = null,
        )

    private fun mockBuildingMessageAndSendingItToQueueForSingleMail() {
        `when`(
            cloudEventMessageHandler.buildCEMessageAndSendToQueue(
                any(), any(), any(), any(), any(),
            ),
        ).then {
            val emailMessage = parseJsonStringIntoEmailMessage(it.getArgument(0))

            if (emailMessage.typedEmailContent !is InternalEmailContentTable) {
                assertEquals(emailMessage.receiver.first(), EmailRecipient.EmailAddress(receiver))
                assertTrue(emailMessage.typedEmailContent is SingleDatasetUploadedEngagement)
                val singleDatasetsUploadedEngagement = emailMessage.typedEmailContent as SingleDatasetUploadedEngagement

                assertEquals(companyName, singleDatasetsUploadedEngagement.companyName)
                assertEquals(companyId.toString(), singleDatasetsUploadedEngagement.companyId)
                assertEquals(readableFrameworkNameMapping.getValue(dataType), singleDatasetsUploadedEngagement.dataTypeLabel)
                assertEquals(reportingPeriod, singleDatasetsUploadedEngagement.reportingPeriod)

                assertEquals(MessageType.SEND_EMAIL, it.getArgument<String>(1))
                assertEquals(correlationId, it.getArgument<String>(2))
                assertEquals(ExchangeName.SEND_EMAIL, it.getArgument<String>(3))
                assertEquals(RoutingKeyNames.EMAIL, it.getArgument<String>(4))
            }
        }
    }

    @Test
    fun `check that external single notification email is correctly send`() {
        mockBuildingMessageAndSendingItToQueueForSingleMail()

        val latestElementaryEvent = initElementaryEvent(dataType, reportingPeriod)

        notificationEmailSender.sendExternalAndInternalNotificationEmail(
            NotificationService.NotificationEmailType.Single,
            latestElementaryEvent, emptyList(),
            companyName, listOf(receiver), correlationId,
        )
    }

    private fun mockBuildingMessageAndSendingItToQueueForSummaryMail() {
        `when`(
            cloudEventMessageHandler.buildCEMessageAndSendToQueue(
                any(), any(), any(), any(), any(),
            ),
        ).then {
            val emailMessage = parseJsonStringIntoEmailMessage(it.getArgument(0))

            if (emailMessage.typedEmailContent !is InternalEmailContentTable) {
                assertEquals(emailMessage.receiver.first(), EmailRecipient.EmailAddress(receiver))
                assertTrue(emailMessage.typedEmailContent is MultipleDatasetsUploadedEngagement)
                val multipleDatasetsUploadedEngagement = emailMessage.typedEmailContent as MultipleDatasetsUploadedEngagement

                assertEquals(companyName, multipleDatasetsUploadedEngagement.companyName)
                assertEquals(companyId.toString(), multipleDatasetsUploadedEngagement.companyId)
                assertEquals(
                    listOf(
                        MultipleDatasetsUploadedEngagement.FrameworkData("LkSG", listOf("2020")),
                        MultipleDatasetsUploadedEngagement.FrameworkData("SFDR", listOf("2021", "2022")),
                        MultipleDatasetsUploadedEngagement.FrameworkData("VSME", listOf("2022")),
                    ),
                    multipleDatasetsUploadedEngagement.frameworkData,
                )
                assertEquals(daysSinceLastNotificationEvent, multipleDatasetsUploadedEngagement.numberOfDays)
                assertEquals(MessageType.SEND_EMAIL, it.getArgument<String>(1))
                assertEquals(correlationId, it.getArgument<String>(2))
                assertEquals(ExchangeName.SEND_EMAIL, it.getArgument<String>(3))
                assertEquals(RoutingKeyNames.EMAIL, it.getArgument<String>(4))
            }
        }
    }

    @Test
    fun `check that external summary notification email is correctly send`() {
        mockBuildingMessageAndSendingItToQueueForSummaryMail()

        val unprocessedElementaryEvents =
            listOf(
                initElementaryEvent(DataTypeEnum.lksg, "2020"),
                initElementaryEvent(DataTypeEnum.sfdr, "2021"),
                initElementaryEvent(DataTypeEnum.vsme, "2022"),
                initElementaryEvent(DataTypeEnum.sfdr, "2022"),
            )

        val latestElementaryEvent = unprocessedElementaryEvents.last()

        notificationEmailSender.sendExternalAndInternalNotificationEmail(
            NotificationService.NotificationEmailType.Summary(daysSinceLastNotificationEvent),
            latestElementaryEvent, unprocessedElementaryEvents,
            companyName, listOf(receiver), correlationId,
        )
    }
}
