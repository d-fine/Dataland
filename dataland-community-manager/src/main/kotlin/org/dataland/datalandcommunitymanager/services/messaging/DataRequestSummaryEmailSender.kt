package org.dataland.datalandcommunitymanager.services.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandcommunitymanager.entities.NotificationEventEntity
import org.dataland.datalandcommunitymanager.events.NotificationEventType
import org.dataland.datalandcommunitymanager.utils.CompanyInfoService
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.email.DataRequestSummary
import org.dataland.datalandmessagequeueutils.messages.email.EmailMessage
import org.dataland.datalandmessagequeueutils.messages.email.EmailRecipient
import org.dataland.datalandmessagequeueutils.messages.email.TypedEmailContent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * A service used to send data request summary emails.
 */
@Service("DataRequestSummaryEmailSender")
class DataRequestSummaryEmailSender(
    @Autowired val cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired private val companyInfoService: CompanyInfoService,
    @Autowired val objectMapper: ObjectMapper,
) {
    /**
     * Sends the Data Request summary email.
     * @param unprocessedEvents A list of notification event entities that are unprocessed
     * and contained in the summary email.
     * @param userId The ID of the user to whom the email should be sent.
     */
    fun sendDataRequestSummaryEmail(
        unprocessedEvents: List<NotificationEventEntity>,
        userId: UUID,
    ) {
        val emailContent = dataRequestSummaryEmailContent(unprocessedEvents)
        val receiver = listOf(EmailRecipient.UserId(userId.toString()))
        val message = EmailMessage(emailContent, receiver, emptyList(), emptyList())
        val correlationId = UUID.randomUUID().toString()
        // Send the email message to the queue
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            objectMapper.writeValueAsString(message),
            MessageType.SEND_EMAIL,
            correlationId,
            ExchangeName.SEND_EMAIL,
            RoutingKeyNames.EMAIL,
        )
    }

    /**
     * Constructs the content of the Data Request Summary email from the provided events.
     * @param events A list of notification event entities to process.
     * @return The email content that encapsulates the summary of data requests.
     */
    private fun dataRequestSummaryEmailContent(events: List<NotificationEventEntity>): TypedEmailContent {
        // Aggregate data for each type of event
        val newData = aggregateFrameworkDataForOneEventType(events, NotificationEventType.AvailableEvent)
        val updatedData = aggregateFrameworkDataForOneEventType(events, NotificationEventType.UpdatedEvent)
        val nonsourceableData = aggregateFrameworkDataForOneEventType(events, NotificationEventType.NonSourceableEvent)
        // Create and return an email content object
        return DataRequestSummary(newData, updatedData, nonsourceableData)
    }

    /**
     * Aggregates framework data for a specific event type.
     * @param events A list of notification event entities to process.
     * @param eventType The type of notification event to filter and aggregate data for.
     * @return A list of aggregated framework data objects.
     */
    private fun aggregateFrameworkDataForOneEventType(
        events: List<NotificationEventEntity>,
        eventType: NotificationEventType,
    ): List<DataRequestSummary.FrameworkData> {
        val filteredEventTypeEvents = events.filter { it.notificationEventType == eventType }
        val groupedEvents = filteredEventTypeEvents.groupBy { Pair(it.framework.toString(), it.reportingPeriod) }

        // Map the grouped events to FrameworkData objects
        return groupedEvents.map { (key, group) ->
            val (dataTypeLabel, reportingPeriod) = key
            // Get unique company names/IDs for the grouped events
            val companies = group.map { companyInfoService.checkIfCompanyIdIsValidAndReturnNameOrId(it.companyId.toString()) }.distinct()
            // Create a FrameworkData object for each group
            DataRequestSummary.FrameworkData(
                dataTypeLabel = dataTypeLabel,
                reportingPeriod = reportingPeriod,
                companies = companies,
            )
        }
    }
}
