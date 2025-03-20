package org.dataland.datalandcommunitymanager.repositories

import org.dataland.datalandcommunitymanager.entities.NotificationEventEntity
import org.dataland.datalandcommunitymanager.events.NotificationEventType
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

/**
 * A JPA repository for storing and retrieving notification events for notification event types
 */
interface NotificationEventRepository : JpaRepository<NotificationEventEntity, UUID> {
    /**
     * A function for searching for unprocessed notification events by companyId and the type of the notification events
     * @param companyId the UUID of the company to filter by
     * @param notificationEventType default set to InvestorRelationshipsEvent
     * @return a list of unprocessed notification events matching the criteria
     */
    fun findAllByCompanyIdAndNotificationEventTypeAndIsProcessedFalse(
        companyId: UUID,
        notificationEventType: NotificationEventType = NotificationEventType.InvestorRelationshipsEvent,
    ): List<NotificationEventEntity>

    /**
     * A function for searching for unprocessed notification events by userId and the type of the notification events
     * @param userId the UUID of the user to filter by.
     * @param notificationEventTypes a list of notification event types to filter by.
     * @return a list of unprocessed notification events matching the criteria.
     */
    fun findAllByUserIdAndNotificationEventTypeInAndIsProcessedFalse(
        userId: UUID,
        notificationEventTypes: List<NotificationEventType>,
    ): List<NotificationEventEntity>
}
