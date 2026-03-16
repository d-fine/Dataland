package org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DataPointJudgement
import java.util.UUID

/**
 * JPA entity storing per-data-point review information within a dataset review.
 */
@Suppress("LongParameterList")
@Entity
@Table(name = "dataset_review_entity_data_point_review_details")
class DataPointJudgementEntity(
    @Id val id: UUID = UUID.randomUUID(),
    val dataPointType: String,
    val dataPointId: UUID?,
    @ManyToMany(cascade = [CascadeType.ALL])
    @JoinTable(
        name = "data_point_judgement_qa_reports",
        joinColumns = [JoinColumn(name = "data_point_judgement_id")],
        inverseJoinColumns = [JoinColumn(name = "qa_report_id")],
    )
    val qaReports: MutableList<DataPointQaReportEntity> = mutableListOf(),
    var acceptedSource: AcceptedDataPointSource?,
    var reporterUserIdOfAcceptedQaReport: UUID?,
    @Column(columnDefinition = "TEXT", nullable = true)
    var customValue: String?,
    @JoinColumn(name = "dataset_judgement_id")
    @ManyToOne
    var datasetJudgement: DatasetJudgementEntity? = null,
) {
    /**
     * Converts this entity to its API response DTO.
     */
    fun toDataPointReviewDetails(): DataPointJudgement =
        DataPointJudgement(
            dataPointType = dataPointType,
            dataPointId = dataPointId,
            qaReports = qaReports.map { it.toQaReportDataPointWithReporterDetails() },
            acceptedSource = acceptedSource,
            reporterUserIdOfAcceptedQaReport = reporterUserIdOfAcceptedQaReport,
            customValue = customValue,
        )
}
