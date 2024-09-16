package org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.ReviewInformationResponse

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
    var message: String?,
) {
    /**
     * Converts the ReviewInformationEntity into a ReviewInformationResponse that is used in a response for a
     * GET Request.
     * The ReviewInformationResponse can optionally hide the reviewerKeycloakId by setting showReviewerKeycloakId
     * to false.
     */
    fun toReviewInformationResponse(showReviewerKeycloakId: Boolean): ReviewInformationResponse {
        return ReviewInformationResponse(
            dataId,
            receptionTime,
            qaStatus,
            if (showReviewerKeycloakId) reviewerKeycloakId else null,
            message,
        )
    }
}
