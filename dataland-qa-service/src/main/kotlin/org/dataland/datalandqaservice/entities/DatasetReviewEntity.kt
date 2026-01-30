package org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.converter.DatasetReviewStateConverter
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetReview
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetReviewState
import java.util.UUID

/**
 * The database entity for storing dataset reviews
 */
@SuppressWarnings("LongParameterList")
@Entity
@Table(name = "dataset_review")
class DatasetReviewEntity(
    @Id
    @Column(name = "dataset_review_id")
    val dataSetReviewId: UUID,
    @Column(name = "dateset_id", unique = true)
    val datasetId: UUID,
    @Column(name = "company_id")
    val companyId: UUID,
    @Column(name = "data_type")
    val dataType: String,
    @Column(name = "reporting_period")
    val reportingPeriod: String,
    @Convert(converter = DatasetReviewStateConverter::class)
    @Column(name = "status")
    var status: DatasetReviewState = DatasetReviewState.Pending,
    @Column(name = "reviewer_user_id")
    var reviewerUserId: UUID,
    @ElementCollection
    @Column(name = "preapproved_data_point_ids")
    var preapprovedDataPointIds: Set<UUID> = emptySet(),
    @OneToMany
    @JoinColumn(name = "dataset_review_id")
    var qaReports: MutableSet<DataPointQaReportEntity>,
    @ElementCollection
    @Column(name = "approved_qa_report_ids")
    var approvedQaReportIds: MutableMap<String, UUID> = mutableMapOf(),
    @ElementCollection
    @Column(name = "approved_data_point_ids")
    var approvedDataPointIds: MutableMap<String, UUID> = mutableMapOf(),
    @ElementCollection
    @Column(name = "approved_custom_data_point_ids")
    var approvedCustomDataPointIds: MutableMap<String, String> = mutableMapOf(),
) {
    /**
     * Convert to DatasetReview objects for API use.
     */
    fun toDatasetReview(): DatasetReview =
        DatasetReview(
            dataSetReviewId,
            datasetId,
            companyId,
            dataType,
            reportingPeriod,
            status,
            reviewerUserId,
            preapprovedDataPointIds,
            qaReports,
            approvedQaReportIds,
            approvedDataPointIds,
            approvedCustomDataPointIds,
        )
}
