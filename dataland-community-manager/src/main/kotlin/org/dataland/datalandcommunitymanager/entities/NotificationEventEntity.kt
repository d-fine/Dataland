package org.dataland.datalandcommunitymanager.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "notification_event")
data class NotificationEventEntity(
    @Id
    @Column(name = "notification_event_id")
    val notificationEventId: String = UUID.randomUUID().toString(),

    @OneToMany
    @Column(columnDefinition = "TEXT")
    val elementaryEvents: List<ElementaryEventEntity>,

    @Column(columnDefinition = "TEXT")
    val companyId: String,

    @Column(columnDefinition = "LONG")
    val creationTimestamp: Long,
) {
    init {
        require(elementaryEvents.isNotEmpty())
    }
}
