package org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.converter.DatasetReviewStateConverter
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetReviewResponse
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetReviewState
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReportIdWithUploaderCompanyId
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
    @Column(name = "dataset_id")
    val datasetId: UUID,
    @Column(name = "company_id")
    val companyId: UUID,
    @Column(name = "data_type")
    val dataType: String,
    @Column(name = "reporting_period")
    val reportingPeriod: String,
    @Convert(converter = DatasetReviewStateConverter::class)
    @Column(name = "review_state")
    var reviewState: DatasetReviewState = DatasetReviewState.Pending,
    @Column(name = "reviewer_user_id")
    var reviewerUserId: UUID?,
    @ElementCollection
    @Column(name = "preapproved_data_point_ids")
    var preapprovedDataPointIds: Set<UUID> = emptySet(),
    @ElementCollection
    @Column(name = "qa_reports")
    var qaReports: Set<QaReportIdWithUploaderCompanyId>,
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
    fun toDatasetReviewResponse(): DatasetReviewResponse =
        DatasetReviewResponse(
            dataSetReviewId.toString(),
            datasetId.toString(),
            companyId.toString(),
            dataType,
            reportingPeriod,
            reviewState,
            reviewerUserId?.toString(),
            null,
            preapprovedDataPointIds.map { it.toString() }.toSet(),
            qaReports.map { it.toString() }.toSet(),
            approvedQaReportIds.mapValues { it.value.toString() },
            approvedDataPointIds.mapValues { it.value.toString() },
            approvedCustomDataPointIds.toMap(),
        )
}
