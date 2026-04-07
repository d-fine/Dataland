package org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.dataland.datalandbackendutils.converter.QaStatusConverter
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.NonSourceableQaReviewInformation
import java.util.UUID

/**
 * Stores QA review information for non-sourceability requests.
 */
@Entity
@Table(name = "non_sourceable_qa_review_information")
data class NonSourceableQaReviewInformationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,
    @Column(name = "non_sourceability_id", nullable = false)
    val nonSourceabilityId: UUID,
    @Column(name = "company_id", nullable = false)
    val companyId: String,
    @Column(name = "data_type", nullable = false)
    val dataType: String,
    @Column(name = "reporting_period", nullable = false)
    val reportingPeriod: String,
    @Column(name = "reason", columnDefinition = "TEXT", nullable = true)
    val reason: String?,
    @Column(name = "uploader_user_id", nullable = false)
    val uploaderUserId: String,
    @Column(name = "upload_time", nullable = false)
    val uploadTime: Long,
    @Convert(converter = QaStatusConverter::class)
    @Column(name = "qa_status", nullable = false)
    var qaStatus: QaStatus,
    @Column(name = "reviewer_user_id", nullable = true)
    var reviewerUserId: String?,
    @Column(name = "qa_comment", columnDefinition = "TEXT", nullable = true)
    var qaComment: String?,
    @Column(name = "created_at", nullable = false)
    val createdAt: Long,
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Long,
) {
    /**
     * Converts the entity to its API model representation.
     */
    fun toApiModel(): NonSourceableQaReviewInformation =
        NonSourceableQaReviewInformation(
            id = id,
            nonSourceabilityId = nonSourceabilityId,
            companyId = companyId,
            dataType = dataType,
            reportingPeriod = reportingPeriod,
            reason = reason,
            uploaderUserId = uploaderUserId,
            uploadTime = uploadTime,
            qaStatus = qaStatus,
            reviewerUserId = reviewerUserId,
            qaComment = qaComment,
        )
}
