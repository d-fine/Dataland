package org.dataland.datalandcommunitymanager.services.elementaryEventProcessing

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandcommunitymanager.entities.ElementaryEventEntity
import org.dataland.datalandcommunitymanager.events.NotificationEventType
import org.dataland.datalandcommunitymanager.model.elementaryEventProcessing.NotificationEventBasicInfo
import org.dataland.datalandcommunitymanager.repositories.NotificationEventRepository
import org.dataland.datalandcommunitymanager.services.NotificationService
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Instant

/**
 * Base class to define basic functionalities required for processing elementary events on message queues
 */
@Component
abstract class BaseEventProcessor(
    @Autowired val notificationService: NotificationService,
    @Autowired val notificationEventRepository: NotificationEventRepository,
    @Autowired val objectMapper: ObjectMapper,
) {
    @Value("\${dataland.community-manager.notification-feature-flag:false}")
    var notificationFeatureFlagAsString: String? = null

    abstract val notificationEventType: NotificationEventType
    abstract val messageType: String
    abstract val actionType: String?
    lateinit var logger: org.slf4j.Logger

    /**
     * Provide functionality inherent to all incoming events like processing and persisting incoming
     * elementary events and potentially triggering notifications
     * Event-specific processing logic needs to be implemented in child classes.
     */
    fun processEvent(
        notificationEventBasicInfo: NotificationEventBasicInfo,
        correlationId: String,
        messageType: String,
    ) {
        if (!isNotificationServiceEnabled()) {
            return
        }

        val visibilityType =
            when (messageType) {
                MessageType.PRIVATE_DATA_RECEIVED -> "private"
                MessageType.QA_STATUS_UPDATED -> "public"
                else -> ""
            }

        logger.info(
            "Processing elementary event: Request for storage of $visibilityType framework data. " +
                "CorrelationId: $correlationId",
        )

        val storedElementaryEvent = createAndSaveElementaryEvent(notificationEventBasicInfo, notificationEventType)

        notificationService.notifyOfElementaryEvents(storedElementaryEvent, correlationId)
    }

    /**
     * Create and persist new elementary event
     */
    protected fun createAndSaveElementaryEvent(
        notificationEventBasicInfo: NotificationEventBasicInfo,
        elementaryEventType: NotificationEventType,
    ): ElementaryEventEntity =
        notificationEventRepository.saveAndFlush(
            ElementaryEventEntity(
                elementaryEventType = elementaryEventType,
                companyId = notificationEventBasicInfo.companyId,
                framework = notificationEventBasicInfo.framework,
                reportingPeriod = notificationEventBasicInfo.reportingPeriod,
                creationTimestamp = Instant.now().toEpochMilli(),
                notificationEvent = null,
            ),
        )

    /**
     * Parses a message payload from the rabbit mq as object.
     * The object mapper itself throws errors if parsing is not possible.
     * @param jsonString the content of the message
     * @returns an object that contains basic info about the elementary event associated with the payload
     */
    fun createElementaryEventBasicInfo(jsonString: String): NotificationEventBasicInfo {
        val temporaryObjectMapper =
            objectMapper
                .copy()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        return temporaryObjectMapper.readValue(jsonString, NotificationEventBasicInfo::class.java)
    }

    /**
     * Returns the app prop setting if the notification service feature shall be enabled or not
     */
    private fun isNotificationServiceEnabled(): Boolean {
        val isNotificationServiceEnabled = notificationFeatureFlagAsString?.toBooleanStrictOrNull() ?: false
        if (!isNotificationServiceEnabled) {
            logger.info("Notification service feature flag is disabled. Skipping elementary event processing.")
        }
        return isNotificationServiceEnabled
    }
}
