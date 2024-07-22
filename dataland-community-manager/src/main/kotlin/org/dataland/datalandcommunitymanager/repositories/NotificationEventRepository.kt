package org.dataland.datalandcommunitymanager.repositories

import org.dataland.datalandcommunitymanager.entities.NotificationEventEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface NotificationEventRepository : JpaRepository<NotificationEventEntity, UUID> {

    fun findNotificationEventByCompanyId(companyId: UUID): List<NotificationEventEntity>
}
