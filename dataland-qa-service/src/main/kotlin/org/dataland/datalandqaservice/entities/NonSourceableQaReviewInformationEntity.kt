package org.dataland.datalandqaservice.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.dataland.datalandbackendutils.model.QaStatus
import java.util.UUID

/**
 * QA service persistence entity for a non-sourceability review task.
 * Created when the backend emits a non-sourceability-created event (FR-004).
 * Updated by the QA decision endpoint (FR-006, FR-007, FR-009).
 *
 * [nonSourceabilityId] is the correlation key linking this record to the backend's
 * [NonSourceabilityInformationEntity].
 */
@Entity
@Table(name = "non_sourceable_qa_review_information")
data class NonSourceableQaReviewInformationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "review_id", nullable = false, updatable = false)
    val reviewId: UUID? = null,
    @Column(name = "non_sourceability_id", nullable = false, unique = true)
    val nonSourceabilityId: String,
    @Column(name = "company_id", nullable = false)
    val companyId: String,
    @Column(name = "data_type", nullable = false)
    val dataType: String,
    @Column(name = "reporting_period", nullable = false)
    val reportingPeriod: String,
    @Enumerated(EnumType.STRING)
    @Column(name = "qa_status", nullable = false)
    var qaStatus: QaStatus,
    @Column(name = "reason", nullable = true)
    val reason: String?,
    @Column(name = "uploader_user_id", nullable = false)
    val uploaderUserId: String,
    @Column(name = "upload_time", nullable = false)
    val uploadTime: Long,
    @Column(name = "reviewer_user_id", nullable = true)
    var reviewerUserId: String? = null,
    @Column(name = "qa_comment", columnDefinition = "TEXT", nullable = true)
    var qaComment: String? = null,
)
