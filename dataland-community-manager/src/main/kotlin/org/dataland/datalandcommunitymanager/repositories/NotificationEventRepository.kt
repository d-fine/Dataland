package org.dataland.datalandcommunitymanager.repositories

import org.dataland.datalandcommunitymanager.entities.NotificationEventEntity
import org.dataland.datalandcommunitymanager.events.ElementaryEventType
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface NotificationEventRepository : JpaRepository<NotificationEventEntity, UUID> {

    fun findNotificationEventByCompanyIdAndElementaryEventType(companyId: UUID, elementaryEventType: ElementaryEventType): List<NotificationEventEntity>
}
