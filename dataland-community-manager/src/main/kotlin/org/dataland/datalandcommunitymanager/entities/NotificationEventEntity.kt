package org.dataland.datalandcommunitymanager.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "notification_event")
data class NotificationEventEntity(
    @Id
    val notificationEventId: String = UUID.randomUUID().toString(),

    @Column(columnDefinition = "TEXT")
    val elementaryEventIds: String,

    @Column(columnDefinition = "LONG")
    val creationTimestamp: Long,
){
    init {
        require(elementaryEventIds.isNotEmpty())
    }
}
