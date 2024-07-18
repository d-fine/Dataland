package org.dataland.datalandcommunitymanager.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.events.EventType
import java.util.UUID

@Entity
@Table(name = "elementary_events")
data class ElementaryEventEntity (
    @Id
    @Column(name = "event_id")
    val elementaryEventId: String = UUID.randomUUID().toString(),

    @Column(name = "event_type")
    @Enumerated(EnumType.STRING)
    val eventType: EventType,

    @Column(name = "company_id")
    val companyId: String,

    @Column(name = "framework")
    @Enumerated(EnumType.STRING)
    val framework: DataTypeEnum,

    //TODO Perhaps remove later
    val creationTimestamp: Long,

    @ManyToOne(optional = true)
    @JoinColumn(name = "notification_event_id")
    var notificationEvent: NotificationEventEntity?,
)
