package org.dataland.datalandcommunitymanager.entities

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID
import org.dataland.datalandcommunitymanager.events.ElementaryEventType

/** The database entity for storing a single notification event in the database */
@Entity
@Table(name = "notification_events")
data class NotificationEventEntity(
  @Id val notificationEventId: UUID = UUID.randomUUID(),
  val companyId: UUID,
  @Enumerated(EnumType.STRING) val elementaryEventType: ElementaryEventType,
  val creationTimestamp: Long,
)
