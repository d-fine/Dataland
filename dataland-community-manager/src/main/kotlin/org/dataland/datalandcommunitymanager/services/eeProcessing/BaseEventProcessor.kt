package org.dataland.datalandcommunitymanager.services.eeProcessing

import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandcommunitymanager.entities.ElementaryEventEntity
import org.dataland.datalandcommunitymanager.events.ElementaryEventType
import org.dataland.datalandcommunitymanager.repositories.ElementaryEventRepository
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import java.time.Instant
import java.util.*

/**
 * Base class to define basic functionalities required for processing elementary events on message queues
 */
abstract class BaseEventProcessor {
    abstract val elementaryEventRepository: ElementaryEventRepository

    /**
     * Process ElementaryEvent and notify NotificationService of unprocessed elementaryEvents with the same
     * ElementaryEventType and corresponding companyId as the event currently processed
     */
    abstract fun processEvent(
        @Payload payload: String,
        @Header(MessageHeaderKey.CorrelationId) correlationId: String,
        @Header(MessageHeaderKey.Type) type: String,
    )

    /**
     * Create and persist new elementary event
     */
    protected fun createAndSaveElementaryEvent(
        dataMetaInfo: DataMetaInformation,
        elementaryEventType: ElementaryEventType,
    ) {
        elementaryEventRepository.saveAndFlush(
            ElementaryEventEntity(
                elementaryEventType = elementaryEventType,
                companyId = UUID.fromString(dataMetaInfo.companyId),
                framework = dataMetaInfo.dataType,
                reportingPeriod = dataMetaInfo.reportingPeriod,
                creationTimestamp = Instant.now().toEpochMilli(),
                notificationEvent = null,
            ),
        )
    }

    /**
     * Retrieve unprocessed elementary events for companyId from ElementaryEventRepository
     * @param companyId companyId of elementaryEvents returned
     * @return List of corresponding elementaryEvents
     */
    protected fun getUnprocessedElementaryEventsForCompany(
        companyId: UUID,
        elementaryEventType: ElementaryEventType,
    ): List<ElementaryEventEntity> =
        elementaryEventRepository.findAllByCompanyIdAndElementaryEventTypeAndNotificationEventIsNull(
            companyId,
            elementaryEventType,
        )
}
