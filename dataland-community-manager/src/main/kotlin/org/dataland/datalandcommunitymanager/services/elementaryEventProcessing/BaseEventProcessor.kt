package org.dataland.datalandcommunitymanager.services.elementaryEventProcessing

import org.dataland.datalandcommunitymanager.entities.ElementaryEventEntity
import org.dataland.datalandcommunitymanager.model.elementaryEventProcessing.ElementaryEventBasicInfo
import org.dataland.datalandcommunitymanager.repositories.ElementaryEventRepository
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*

/**
 * Base class to define basic functionalities required for processing elementary events on message queues
 */
@Component
abstract class BaseEventProcessor {
    abstract val elementaryEventRepository: ElementaryEventRepository
    abstract val logger: org.slf4j.Logger

    @Value("\${dataland.community-manager.notification-feature-flag:false}")
    var notificationFeatureFlagString: String? = null
    final val notificationFeatureFlag: Boolean = notificationFeatureFlagString?.toBooleanStrictOrNull() ?: false

    /**
     * Rabbit-MQ listener function to handle incoming elementary events
     */
    abstract fun processEvent(
        @Payload payload: String,
        @Header(MessageHeaderKey.CorrelationId) correlationId: String,
        @Header(MessageHeaderKey.Type) type: String,
    )

    /**
     * Actual processing logic to persists incoming elementary events and potentially trigger notifications
     */
    abstract fun runProcessingLogic(payload: String, correlationId: String, type: String)

    /**
     * Checks if the feature flag is enabled, and if yes, it executes the processingLogic passed to it.
     */
    fun runProcessingLogicIfFeatureFlagEnabled(
        payload: String,
        correlationId: String,
        type: String,
    ) {
        if (!isNotificationServiceEnabled()) {
            return
        }
        runProcessingLogic(payload, correlationId, type)
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
    ): ElementaryEventEntity {
        return elementaryEventRepository.saveAndFlush(
            ElementaryEventEntity(
                elementaryEventType = elementaryEventBasicInfo.elementaryEventType,
                companyId = elementaryEventBasicInfo.companyId,
                framework = elementaryEventBasicInfo.framework,
                reportingPeriod = elementaryEventBasicInfo.reportingPeriod,
                creationTimestamp = Instant.now().toEpochMilli(),
                notificationEvent = null,
            ),
        )
    }
}
