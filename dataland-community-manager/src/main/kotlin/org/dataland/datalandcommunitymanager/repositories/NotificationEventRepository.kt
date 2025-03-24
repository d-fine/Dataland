package org.dataland.datalandcommunitymanager.repositories

import org.dataland.datalandcommunitymanager.entities.NotificationEventEntity
import org.dataland.datalandcommunitymanager.events.NotificationEventType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

/**
 * A JPA repository for storing and retrieving notification events for notification event types
 */
interface NotificationEventRepository : JpaRepository<NotificationEventEntity, UUID> {

    /**
     * A function for searching for unprocessed notification events by a list of notification event types.
     * @param notificationEventTypes a list of notification event types to filter.
     * @return a list of unprocessed notification events matching the criteria.
     */
    @Query("SELECT n FROM NotificationEventEntity n WHERE n.notificationEventType IN :notificationEventTypes AND n.isProcessed = false")
    fun findAllByNotificationEventTypesAndIsProcessedFalse(
        @Param("notificationEventTypes") notificationEventTypes: List<NotificationEventType>
    ): List<NotificationEventEntity>
}
