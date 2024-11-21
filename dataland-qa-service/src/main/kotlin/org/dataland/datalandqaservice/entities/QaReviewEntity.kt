package org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.dataland.datalandbackendutils.model.QaStatus
import java.util.UUID

/**
 * The entity storing the information of a dataset which is gathered during the review or any update of the QA status
 */
@Entity
@Table(name = "qa_review")
data class QaReviewEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val eventId: UUID? = null,
    @Column(name = "data_id", nullable = false)
    val dataId: String,
    @Column(name = "company_id", nullable = false)
    val companyId: String,
    @Column(name = "company_name", nullable = false)
    val companyName: String,
    @Column(name = "data_type", nullable = false)
    val dataType: String,
    @Column(name = "reporting_period", nullable = false)
    val reportingPeriod: String,
    @Column(name = "timestamp", nullable = false)
    val timestamp: Long,
    @Column(name = "qa_status", nullable = false)
    var qaStatus: QaStatus,
    @Column(name = "reviewer_id", nullable = false)
    val triggeringUserId: String,
    @Column(name = "comment", columnDefinition = "TEXT", nullable = true)
    val comment: String?,
)
