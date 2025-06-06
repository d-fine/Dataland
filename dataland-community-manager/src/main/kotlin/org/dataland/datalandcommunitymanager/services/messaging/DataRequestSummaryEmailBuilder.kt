package org.dataland.datalandcommunitymanager.services.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandcommunitymanager.entities.NotificationEventEntity
import org.dataland.datalandcommunitymanager.events.NotificationEventType
import org.dataland.datalandcommunitymanager.utils.CompanyInfoService
import org.dataland.datalandcommunitymanager.utils.readableFrameworkNameMapping
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.email.DataRequestSummaryEmailContent
import org.dataland.datalandmessagequeueutils.messages.email.EmailMessage
import org.dataland.datalandmessagequeueutils.messages.email.EmailRecipient
import org.dataland.datalandmessagequeueutils.messages.email.TypedEmailContent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * A service used to build scheduled data request summary emails, containing all data requests updates for one user
 */
@Service("DataRequestSummaryEmailBuilder")
class DataRequestSummaryEmailBuilder
    @Autowired
    constructor(
        private val cloudEventMessageHandler: CloudEventMessageHandler,
        private val companyInfoService: CompanyInfoService,
        private val objectMapper: ObjectMapper,
    ) {
        /**
         * Builds the Data Requests Summary email and sends CE message.
         * @param unprocessedEvents A list of notification event entities that are unprocessed
         * and contained in the summary email.
         * @param userId The ID of the user to whom the email should be sent.
         */
        fun buildDataRequestSummaryEmailAndSendCEMessage(
            unprocessedEvents: List<NotificationEventEntity>,
            userId: UUID,
        ) {
            val emailContent = dataRequestSummaryEmailContent(unprocessedEvents)
            val receiver = listOf(EmailRecipient.UserId(userId.toString()))
            val message = EmailMessage(emailContent, receiver, emptyList(), emptyList())
            cloudEventMessageHandler.buildCEMessageAndSendToQueue(
                objectMapper.writeValueAsString(message),
                MessageType.SEND_EMAIL,
                correlationId = UUID.randomUUID().toString(),
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
            val newData = aggregateFrameworkDataForOneEventType(events, NotificationEventType.AvailableEvent)
            val updatedData = aggregateFrameworkDataForOneEventType(events, NotificationEventType.UpdatedEvent)
            val nonSourceableData = aggregateFrameworkDataForOneEventType(events, NotificationEventType.NonSourceableEvent)
            return DataRequestSummaryEmailContent(newData, updatedData, nonSourceableData)
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
        ): List<DataRequestSummaryEmailContent.FrameworkData> {
            val filteredEventTypeEvents = events.filter { it.notificationEventType == eventType }
            val groupedEvents = filteredEventTypeEvents.groupBy { Pair(it.framework, it.reportingPeriod) }

            return groupedEvents.map { (key, group) ->
                val (dataType, reportingPeriod) = key
                val companies = group.map { companyInfoService.getValidCompanyNameOrId(it.companyId.toString()) }.distinct()
                DataRequestSummaryEmailContent.FrameworkData(
                    dataTypeLabel = readableFrameworkNameMapping[dataType] ?: dataType.toString(),
                    reportingPeriod = reportingPeriod,
                    companies = companies,
                )
            }
        }
    }
