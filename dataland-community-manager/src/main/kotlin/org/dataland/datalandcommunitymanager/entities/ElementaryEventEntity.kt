package org.dataland.datalandcommunitymanager.entities

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.events.ElementaryEventType
import java.util.UUID

/**
 * The database entity for storing a single elementary event in the database
 */
@Entity
@Table(name = "elementary_events")
data class ElementaryEventEntity(
    @Id
    val elementaryEventId: UUID = UUID.randomUUID(),
    @Enumerated(EnumType.STRING)
    val elementaryEventType: ElementaryEventType,
    val companyId: UUID,
    @Enumerated(EnumType.STRING)
    val framework: DataTypeEnum,
    val reportingPeriod: String,
    val creationTimestamp: Long,
    @ManyToOne(optional = true)
    @JoinColumn(name = "notification_event_id")
    var notificationEvent: NotificationEventEntity?,
)
