package org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DataPointReviewDetails
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource
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
    var companyIdOfAcceptedQaReport: UUID?,
    var customValue: String?,
    @JoinColumn(name = "dataset_review_id")
    @ManyToOne
    val datasetReview: DatasetReviewEntity?,
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
            companyIdOfAcceptedQaReport = companyIdOfAcceptedQaReport,
            customValue = customValue,
        )
}
