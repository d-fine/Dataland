package org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.dataland.datalandbackendutils.model.QaStatus

/**
 * The entity storing the information gathered during the review of a dataset
 */
@Entity
@Table(name = "review_information")
data class ReviewInformationEntity(
    @Id
    val dataId: String,
    val receptionTime: Long,
    var qaStatus: QaStatus,
    val reviewerKeycloakId: String,
)
