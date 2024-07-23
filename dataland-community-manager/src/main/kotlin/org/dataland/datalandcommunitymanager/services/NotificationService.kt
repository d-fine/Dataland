package org.dataland.datalandcommunitymanager.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandcommunitymanager.entities.ElementaryEventEntity
import org.dataland.datalandcommunitymanager.entities.NotificationEventEntity
import org.dataland.datalandcommunitymanager.events.ElementaryEventType
import org.dataland.datalandcommunitymanager.repositories.ElementaryEventRepository
import org.dataland.datalandcommunitymanager.repositories.NotificationEventRepository
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.TemplateEmailMessage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant
import java.util.*

/**
 * Service that handles creation of notification events and sending notifications to interested parties
 * in case of elementary events
 */
@Service("NotificationService")
class NotificationService
@Suppress("LongParameterList")
constructor(
    @Autowired var cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired var notificationEventRepository: NotificationEventRepository,
    @Autowired var elementaryEventRepository: ElementaryEventRepository,
    @Autowired var companyDataControllerApi: CompanyDataControllerApi,
    @Autowired var objectMapper: ObjectMapper,
    @Value("\${dataland.community-manager.notification-threshold-days:30}")
    private val notificationThresholdDays: Int,
    @Value("\${dataland.community-manager.notification-elementaryevents-threshold:10}")
    private val elementaryEventsThreshold: Int,
    @Value("\${dataland.community-manager.proxy-primary-url:local-dev.dataland.com}")
    private val proxyPrimaryUrl: String,
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Enum that contains all possible types of notification emails that might be triggered.
     */
    enum class NotificationEmailType { Single, Summary }

    /**
     * Checks if notification event shall be created or not.
     * If yes, it creates it and sends a message to the queue to trigger notification emails.
     */
    @Transactional
    fun notifyOfElementaryEvents(elementaryEvents: List<ElementaryEventEntity>, correlationId: String) {
        val notificationEmailType =
            checkNotificationRequirementsAndDetermineNotificationEmailType(elementaryEvents)

        if (notificationEmailType != null) {
            logger.info(
                "Requirements for notification event are met. " +
                    "Creating notification event and sending notification emails. " +
                    "CorrelationId: $correlationId",
            )
            createNotificationEventAndReferenceIt(elementaryEvents)
            sendEmailMessageToQueue(notificationEmailType, elementaryEvents, correlationId)
        }
    }

    /**
     * Checks if the requirements for creating a notification event are met.
     * If yes, it returns the type of notification mail that shall be sent.
     * Else it simply returns null.
     */
    fun checkNotificationRequirementsAndDetermineNotificationEmailType(
        elementaryEvents: List<ElementaryEventEntity>,
    ): NotificationEmailType? {
        val companyIdOfEvents = elementaryEvents.first().companyId

        val isLastNotificationEventOlderThanThreshold =
            isLastNotificationEventOlderThanThreshold(companyIdOfEvents, ElementaryEventType.UploadEvent)

        return when {
            isLastNotificationEventOlderThanThreshold && elementaryEvents.size == 1 ->
                NotificationEmailType.Single

            isLastNotificationEventOlderThanThreshold ||
                elementaryEvents.size >= elementaryEventsThreshold ->
                NotificationEmailType.Summary

            else -> null
        }
    }

    /**
     * Creates and persists a new notification event and also puts the reference to this newly created notification
     * event into the associated elementary events.
     */
    private fun createNotificationEventAndReferenceIt(elementaryEvents: List<ElementaryEventEntity>) {
        val notificationEventToStore = NotificationEventEntity(
            companyId = elementaryEvents.first().companyId,
            elementaryEventType = elementaryEvents.first().elementaryEventType,
            creationTimestamp = Instant.now().toEpochMilli(),
            elementaryEvents = elementaryEvents,
        )
        val savedNotificationEvent = notificationEventRepository.saveAndFlush(notificationEventToStore)
        elementaryEvents.forEach {
            it.notificationEvent = savedNotificationEvent
            elementaryEventRepository.saveAndFlush(it)
        }
    }

    /**
     * decides which type of email notification message to send and delegates sending to corresponding method
     */
    private fun sendEmailMessageToQueue(
        notificationEmailType: NotificationEmailType,
        elementaryEvents: List<ElementaryEventEntity>,
        correlationId: String,
    ) {
        when (notificationEmailType) {
            NotificationEmailType.Single -> {
                sendSingleEmailMessageToQueue(elementaryEvents.first(), correlationId)
            }
            NotificationEmailType.Summary -> {
                sendSummaryEmailMessageToQueue(elementaryEvents, correlationId)
            }
        }
    }

    /**
     * Sends singleNotification Template Email Message to Queue
     */
    private fun sendSingleEmailMessageToQueue(
        elementaryEvent: ElementaryEventEntity,
        correlationId: String,
    ) {
        val companyInfo = companyDataControllerApi.getCompanyInfo(elementaryEvent.companyId.toString())
        val properties = mapOf(
            "companyName" to companyInfo.companyName,
            "companyId" to elementaryEvent.companyId.toString(),
            "framework" to elementaryEvent.framework.toString(),
            "year" to elementaryEvent.reportingPeriod,
            "baseUrl" to proxyPrimaryUrl,
        )

        companyInfo.companyContactDetails?.forEach {
                contactAddress ->
            val message = TemplateEmailMessage(
                emailTemplateType = TemplateEmailMessage.Type.SingleNotification,
                receiver = TemplateEmailMessage.EmailAddressEmailRecipient(contactAddress),
                properties = properties,
            )
            cloudEventMessageHandler.buildCEMessageAndSendToQueue(
                objectMapper.writeValueAsString(message),
                MessageType.SendTemplateEmail,
                correlationId,
                ExchangeName.SendEmail,
                RoutingKeyNames.templateEmail,
            )
        }
    }

    private fun sendSummaryEmailMessageToQueue(
        elementaryEvents: List<ElementaryEventEntity>,
        correlationId: String,
    ) {
        val companyInfo = companyDataControllerApi.getCompanyInfo(elementaryEvents.first().companyId.toString())
        val firstElementaryEvent = elementaryEvents.first()
        val properties = mapOf(
            "companyName" to companyInfo.companyName,
            "companyId" to firstElementaryEvent.companyId.toString(),
            "frameworks" to createFrameworkAndYearStringFromElementaryEvents(elementaryEvents),
            "baseUrl" to proxyPrimaryUrl,
            "numberOfDays" to getTimePassedSinceLastNotificationEvent(
                firstElementaryEvent.companyId, firstElementaryEvent.elementaryEventType,
            ).toString(),
        )

        companyInfo.companyContactDetails?.forEach {
                contactAddress ->
            val message = TemplateEmailMessage(
                emailTemplateType = TemplateEmailMessage.Type.SummaryNotification,
                receiver = TemplateEmailMessage.EmailAddressEmailRecipient(contactAddress),
                properties = properties,
            )
            cloudEventMessageHandler.buildCEMessageAndSendToQueue(
                objectMapper.writeValueAsString(message),
                MessageType.SendTemplateEmail,
                correlationId,
                ExchangeName.SendEmail,
                RoutingKeyNames.templateEmail,
            )
        }
    }

    /**
     * Gets last notification event for a specific company
     * @param companyId TODO
     * @param elementaryEventType TODO
     * @return last notificationEvent (null if no previous notification event for this company exists)
     */
    private fun getLastNotificationEventOrNull(
        companyId: UUID,
        elementaryEventType: ElementaryEventType,
    ): NotificationEventEntity? {
        return notificationEventRepository.findNotificationEventByCompanyIdAndElementaryEventType(
            companyId,
            elementaryEventType,
        )
            .maxByOrNull { it.creationTimestamp }
    }

    /**
     * Gets time passed in days since last notification event for a specific company
     * @param companyId TODO
     * @return time passed in days as Int
     */
    private fun getTimePassedSinceLastNotificationEvent(
        companyId: UUID,
        elementaryEventType: ElementaryEventType,
    ): Long {
        val lastNotificationEvent = getLastNotificationEventOrNull(companyId, elementaryEventType)
        return if (lastNotificationEvent == null) {
            elementaryEventsThreshold.toLong()
        } else {
            Duration.between(Instant.ofEpochMilli(lastNotificationEvent.creationTimestamp), Instant.now()).toDays()
        }
    }

    /**
     * Checks if last notification event for company is older than threshold in days
     * @param companyId
     * @return if last notification event for company is older than threshold in days
     */
    private fun isLastNotificationEventOlderThanThreshold(
        companyId: UUID,
        elementaryEventType: ElementaryEventType,
    ): Boolean {
        val lastNotificationEvent = getLastNotificationEventOrNull(companyId, elementaryEventType)
        return lastNotificationEvent == null ||
            Duration.between(Instant.ofEpochMilli(lastNotificationEvent.creationTimestamp), Instant.now())
                .toDays() > notificationThresholdDays
    }

    private fun createFrameworkAndYearStringFromElementaryEvents(
        elementaryEvents: List<ElementaryEventEntity>,
    ): String {
        val frameworkAndYears = elementaryEvents.groupBy(
            keySelector = { it.framework },
            valueTransform = { it.reportingPeriod },
        ).mapValues { (_, years) -> years.sorted() }
        return frameworkAndYears.entries.joinToString(", ") {
                (framework, years) ->
            "$framework: ${years.joinToString(" ") }"
        }
    }
}
