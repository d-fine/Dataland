package org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

/**
 * The entity storing the information about a reviewable dataset
 */
@Entity
@Table(name = "review_queue")
data class ReviewQueueEntity(
    @Id
    val dataId: String,
    val receptionTime: Long,
)
