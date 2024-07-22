package org.dataland.datalandcommunitymanager.entities

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.dataland.datalandcommunitymanager.events.ElementaryEventType
import java.util.UUID

@Entity
@Table(name = "notification_events")
data class NotificationEventEntity(
    @Id
    val notificationEventId: UUID = UUID.randomUUID(),

    val companyId: UUID,

    @Enumerated(EnumType.STRING)
    val elementaryEventType: ElementaryEventType,

    val creationTimestamp: Long,

    @OneToMany(mappedBy = "notificationEvent")
    val elementaryEvents: List<ElementaryEventEntity>,
) {
    init {
        require(elementaryEvents.isNotEmpty())
    }
}
