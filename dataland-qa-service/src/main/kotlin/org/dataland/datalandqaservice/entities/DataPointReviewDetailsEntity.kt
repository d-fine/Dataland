package org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DataPointReviewDetails
import java.util.UUID

/**
 * JPA entity storing per-data-point review information within a dataset review.
 */
@Suppress("LongParameterList")
@Entity
@Table(name = "dataset_review_entity_data_point_review_details")
class DataPointReviewDetailsEntity(
    @Id val id: UUID = UUID.randomUUID(),
    val dataPointType: String,
    val dataPointId: UUID?,
    @OneToMany(mappedBy = "dataPointReviewDetails", cascade = [CascadeType.ALL])
    val qaReports: MutableList<QaReportDataPointWithReporterDetailsEntity> = mutableListOf(),
    var acceptedSource: AcceptedDataPointSource?,
    var reporterUserIdOfAcceptedQaReport: UUID?,
    var companyIdOfAcceptedQaReport: UUID?,
    var customValue: String?,
    @JoinColumn(name = "dataset_review_id")
    @ManyToOne
    var datasetReview: DatasetReviewEntity? = null,
) {
    /**
     * Converts this entity to its API response DTO.
     */
    fun toDataPointReviewDetails(): DataPointReviewDetails =
        DataPointReviewDetails(
            dataPointType = dataPointType,
            dataPointId = dataPointId,
            qaReports = qaReports.map { it.toQaReportDataPointWithReporterDetails() },
            acceptedSource = acceptedSource,
            reporterUserIdOfAcceptedQaReport = reporterUserIdOfAcceptedQaReport,
            companyIdOfAcceptedQaReport = companyIdOfAcceptedQaReport,
            customValue = customValue,
        )

    /**
     * Add an associated request to this data sourcing entity.
     * Make sure the data sourcing entity is also added to the request.
     */
    fun addAssociatedQaReports(qaReport: QaReportDataPointWithReporterDetailsEntity) {
        qaReports.add(qaReport)
        qaReport.dataPointReviewDetails = this
    }
}
