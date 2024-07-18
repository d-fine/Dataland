package org.dataland.datalandcommunitymanager.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.events.EventType
import java.util.UUID

@Entity
@Table(name = "elementary_events")
data class ElementaryEventEntity (
    @Id
    val elementaryEventId: String = UUID.randomUUID().toString(),

    @Column(columnDefinition = "TEXT")
    val eventType: EventType,

    @Column(columnDefinition = "TEXT")
    val companyId: String,

    @Column(columnDefinition = "TEXT")
    val framework: DataTypeEnum,

    @Column(columnDefinition = "LONG")
    val creationTimestamp: Long,
)
