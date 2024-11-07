package org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.model.QaStatus
import java.util.UUID

/**
 * The entity storing the information of a dataset which is gathered during the review or any update of the QA status
 */
@Entity
@Table(name = "dataset_qa_review_log")
data class DatasetQaReviewLogEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val eventId: UUID? = null,
    @Column(name = "data_id")
    val dataId: String,
    @Column(name = "company_id")
    val companyId: String,
    @Column(name = "company_name")
    val companyName: String,
    @Column(name = "data_type")
    val dataType: DataTypeEnum,
    @Column(name = "reporting_period")
    val reportingPeriod: String,
    @Column(name = "timestamp")
    val timestamp: Long,
    @Column(name = "qa_status")
    var qaStatus: QaStatus,
    @Column(name = "reviewer_id")
    val reviewerId: String,
    @Column(name = "comment", columnDefinition = "TEXT")
    val comment: String?,
)
