package org.dataland.datalandcommunitymanager.repositories

import java.util.UUID
import org.dataland.datalandcommunitymanager.entities.NotificationEventEntity
import org.dataland.datalandcommunitymanager.events.ElementaryEventType
import org.springframework.data.jpa.repository.JpaRepository

/** A JPA repository for storing and retrieving notification events for elementary event types */
interface NotificationEventRepository : JpaRepository<NotificationEventEntity, UUID> {

  /**
   * A function for searching for notification events by companyId and the type of the associated
   * elementary events
   *
   * @param companyId to filter for
   * @param elementaryEventType to filter for
   * @returns the notification events
   */
  fun findNotificationEventByCompanyIdAndElementaryEventType(
    companyId: UUID,
    elementaryEventType: ElementaryEventType,
  ): List<NotificationEventEntity>
}
