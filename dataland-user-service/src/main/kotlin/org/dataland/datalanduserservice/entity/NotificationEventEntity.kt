package org.dataland.datalanduserservice.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalanduserservice.model.enums.NotificationEventType
import java.time.Instant
import java.util.UUID

/**
 * The database entity for storing a single notification event in the database
 */
@Entity
@Table(
    name = "notification_events",
    indexes = [Index(name = "idx_notification_events", columnList = "notificationEventType")],
)
data class NotificationEventEntity(
    @Id
    val notificationEventId: UUID = UUID.randomUUID(),
    @Enumerated(EnumType.STRING)
    val notificationEventType: NotificationEventType,
    val userId: UUID,
    val companyId: UUID,
    val framework: DataTypeEnum,
    val reportingPeriod: String,
    val creationTimestamp: Long = Instant.now().toEpochMilli(),
)
