package org.dataland.datalandqaservice.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.dataland.datalandbackendutils.model.QaStatus
import java.util.UUID

/**
 * QA-side review projection for backend non-sourceability lifecycle records.
 */
@Entity
@Table(
    name = "non_sourceable_qa_review_information",
    indexes = [
        Index(name = "idx_non_sourceable_qa_status", columnList = "qa_status"),
        Index(name = "idx_non_sourceable_qa_dimensions", columnList = "company_id,data_type,reporting_period"),
    ],
)
data class NonSourceableQaReviewInformationEntity(
    @Id
    @Column(name = "non_sourceability_id", nullable = false, updatable = false)
    val nonSourceabilityId: UUID,
    @Column(name = "company_id", nullable = false)
    val companyId: String,
    @Column(name = "data_type", nullable = false)
    val dataType: String,
    @Column(name = "reporting_period", nullable = false)
    val reportingPeriod: String,
    @Enumerated(EnumType.STRING)
    @Column(name = "qa_status", nullable = false)
    var qaStatus: QaStatus,
    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    val reason: String,
    @Column(name = "uploader_user_id", nullable = false)
    val uploaderUserId: String,
    @Column(name = "upload_time", nullable = false)
    val uploadTime: Long,
    @Column(name = "reviewer_user_id")
    var reviewerUserId: String? = null,
    @Column(name = "qa_comment", columnDefinition = "TEXT")
    var qaComment: String? = null,
)
