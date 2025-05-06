package org.dataland.datalandcommunitymanager.entities

import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.converters.NotificationEventTypeAttributeConverter
import org.dataland.datalandcommunitymanager.events.NotificationEventType
import java.time.Instant
import java.util.UUID

/**
 * The database entity for storing a single notification event in the database
 */
@Entity
@Table(
    name = "notification_events",
    indexes = [Index(name = "idx_notification_events", columnList = "isProcessed, notificationEventType")],
)
data class NotificationEventEntity(
    @Id
    val notificationEventId: UUID = UUID.randomUUID(),
    @Convert(converter = NotificationEventTypeAttributeConverter::class)
    val notificationEventType: NotificationEventType,
    val userId: UUID? = null,
    var isProcessed: Boolean,
    val companyId: UUID,
    val framework: DataTypeEnum,
    val reportingPeriod: String,
    val creationTimestamp: Long = Instant.now().toEpochMilli(),
)
