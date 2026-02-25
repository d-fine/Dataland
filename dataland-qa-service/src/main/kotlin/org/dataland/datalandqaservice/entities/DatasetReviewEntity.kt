package org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.converters.DatasetReviewStateConverter
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DataPointReviewDetails
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetReviewResponse
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetReviewState
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReporterCompany
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
    @Column(name = "review_state")
    @Convert(converter = DatasetReviewStateConverter::class)
    var reviewState: DatasetReviewState = DatasetReviewState.Pending,
    @Column(name = "reviewer_user_id")
    var reviewerUserId: UUID,
    @Column(name = "reviewer_user_name")
    var reviewerUserName: String,
    @ElementCollection
    @Column(name = "qa_reporter_companies")
    var qaReporterCompanies: MutableList<QaReporterCompany>,
    @OneToMany(mappedBy = "datasetReview", cascade = [CascadeType.ALL])
    val dataPoints: Map<String, DataPointReviewDetails>,
) {
    /**
     * Convert to DatasetReview objects for API use.
     */
    fun toDatasetReviewResponse(): DatasetReviewResponse =
        // qa report IDs
        //
        DatasetReviewResponse(
            dataSetReviewId.toString(),
            datasetId.toString(),
            companyId.toString(),
            dataType,
            reportingPeriod,
            reviewState,
            reviewerUserId.toString(),
            reviewerUserName,
            qaReporterCompanies,
            dataPoints,
        )
}
