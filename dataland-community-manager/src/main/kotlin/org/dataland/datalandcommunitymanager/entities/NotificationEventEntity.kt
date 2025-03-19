package org.dataland.datalandcommunitymanager.entities

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.events.NotificationEventType
import java.time.Instant
import java.util.UUID

/**
 * The database entity for storing a single notification event in the database
 */
@Entity
@Table(name = "notification_events")
data class NotificationEventEntity(
    @Id
    val notificationEventId: UUID = UUID.randomUUID(),
    @Enumerated(EnumType.STRING)
    val notificationEventType: NotificationEventType,
    val userId: UUID? = null,
    val isProcessed: Boolean,
    val companyId: UUID,
    val framework: DataTypeEnum,
    val reportingPeriod: String,
    val creationTimestamp: Long = Instant.now().toEpochMilli(),
)
