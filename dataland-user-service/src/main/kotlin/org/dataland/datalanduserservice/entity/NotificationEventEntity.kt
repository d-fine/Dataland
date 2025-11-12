package org.dataland.datalanduserservice.entity

import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalanduserservice.converter.DataTypeEnumConverter
import org.dataland.datalanduserservice.converter.NotificationEventTypeAttributeConverter
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
    @Convert(NotificationEventTypeAttributeConverter::class)
    val notificationEventType: NotificationEventType,
    val companyId: UUID,
    @Convert(DataTypeEnumConverter::class)
    val framework: DataTypeEnum,
    val reportingPeriod: String,
    val creationTimestamp: Long = Instant.now().toEpochMilli(),
)
