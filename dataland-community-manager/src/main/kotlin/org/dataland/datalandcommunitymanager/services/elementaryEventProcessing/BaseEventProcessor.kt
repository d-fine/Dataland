package org.dataland.datalandcommunitymanager.services.elementaryEventProcessing

import org.dataland.datalandcommunitymanager.entities.ElementaryEventEntity
import org.dataland.datalandcommunitymanager.events.ElementaryEventType
import org.dataland.datalandcommunitymanager.model.elementaryEventProcessing.ElementaryEventBasicInfo
import org.dataland.datalandcommunitymanager.repositories.ElementaryEventRepository
import org.dataland.datalandcommunitymanager.services.NotificationService
import org.dataland.datalandcommunitymanager.utils.PayloadValidator.validatePayloadAndReturnElementaryEventBasicInfo
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import java.time.Instant

/**
 * Base class to define basic functionalities required for processing elementary events on message queues
 */
@Component
abstract class BaseEventProcessor(
    @Autowired val messageUtils: MessageQueueUtils,
    @Autowired val notificationService: NotificationService,
    @Autowired val elementaryEventRepository: ElementaryEventRepository,
) {
    @Value("\${dataland.community-manager.notification-feature-flag:false}")
    var notificationFeatureFlagString: String? = null
    final val notificationFeatureFlag: Boolean = notificationFeatureFlagString?.toBooleanStrictOrNull() ?: false

    lateinit var elementaryEventType: ElementaryEventType
    lateinit var messageType: String
    lateinit var actionType: String
    lateinit var logger: org.slf4j.Logger

    /**
     * Rabbit-MQ listener function to handle incoming elementary events.
     * Processes and persists incoming elementary events and potentially triggers notifications
     */
    fun processEvent(
        @Payload payload: String,
        @Header(MessageHeaderKey.CorrelationId) correlationId: String,
        @Header(MessageHeaderKey.Type) type: String,
    ) {
        if (!isNotificationServiceEnabled()) {
            return
        }

        messageUtils.validateMessageType(type, messageType)

        val elementaryEventMetaInfo =
            validatePayloadAndReturnElementaryEventBasicInfo(payload, actionType)

        val privateOrPublic = when (messageType) {
            MessageType.PrivateDataReceived -> "private"
            MessageType.PublicDataReceived -> "public"
            else -> ""
        }

        logger.info(
            "Processing elementary event: Request for storage of $privateOrPublic framework data. " +
                "CorrelationId: $correlationId",
        )

        val storedElementaryEvent = createAndSaveElementaryEvent(elementaryEventMetaInfo, elementaryEventType)

        notificationService.notifyOfElementaryEvents(storedElementaryEvent, correlationId)
    }

    /**
     * Returns the app prop setting if the notification service feature shall be enabled or not
     */
    private fun isNotificationServiceEnabled(): Boolean {
        return notificationFeatureFlag.also {
            if (!it) logger.info("Notification service feature flag is disabled. Skipping elementary event processing.")
        }
    }

    /**
     * Create and persist new elementary event
     */
    protected fun createAndSaveElementaryEvent(
        elementaryEventBasicInfo: ElementaryEventBasicInfo,
        elementaryEventType: ElementaryEventType,
    ): ElementaryEventEntity {
        return elementaryEventRepository.saveAndFlush(
            ElementaryEventEntity(
                elementaryEventType = elementaryEventType,
                companyId = elementaryEventBasicInfo.companyId,
                framework = elementaryEventBasicInfo.framework,
                reportingPeriod = elementaryEventBasicInfo.reportingPeriod,
                creationTimestamp = Instant.now().toEpochMilli(),
                notificationEvent = null,
            ),
        )
    }
}
