package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DatasetReviewEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReportDataPointWithReporterDetails
import java.util.UUID

/**
 * Data class which includes relevant information for the review process.
 * @property dataPointTypeId the identifier for the type of the data point
 * @property dataPointId the identifier for the specific data point
 * @property qaReport the QA report data point associated with this review overview
 * @property acceptedSource the source from which the accepted value for this data point was derived
 * @property customValue a custom value for this data point if applicable
 */
@Entity
@Table(name = "dataset_review_entity_data_point_review_details")
data class DataPointReviewDetails(
    @Id val dataPointType: String,
    val dataPointId: UUID?,
    @OneToMany(mappedBy = "dataPointReviewDetails", cascade = [CascadeType.ALL])
    val qaReports: List<QaReportDataPointWithReporterDetails> = listOf(),
    var acceptedSource: AcceptedDataPointSource?,
    var companyIdOfAcceptedQaReport: UUID?,
    var customValue: String?,
    @JoinColumn(name = "data_point_review_details_id")
    @ManyToOne
    val datasetReview: DatasetReviewEntity?,
)
