package org.dataland.datalandcommunitymanager.repositories

import org.dataland.datalandcommunitymanager.entities.NotificationEventEntity
import org.springframework.data.jpa.repository.JpaRepository

interface NotificationEventRepository : JpaRepository<NotificationEventEntity, String> {
}