package org.dataland.datalandqaservice.entities

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.dataland.datalandbackendutils.model.QAStatus

/**
 * The entity storing the review status of a dataset
 */
@Entity
@Table(name = "dataset_review_status")
data class DatasetReviewStatusEntity(
    @Id
    val dataId: String,
    val correlationId: String,
    var qaStatus: QAStatus,
    val receptionTime: Long,
    val reviewerKeycloakId: String? = null,
)
