package org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.converters.PreApprovalCheckResultsConverter
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DataPointJudgement
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalCheckResults
import java.util.UUID

/**
 * JPA entity storing per-data-point information within a dataset judgement.
 */
@Suppress("LongParameterList")
@Entity
@Table(
    name = "dataset_judgement_entity_data_point_judgement",
    indexes = [Index(name = "idx_datapoint_dataset_judgment_id", columnList = "dataset_judgement_id")],
)
class DataPointJudgementEntity(
    @Id val id: UUID = UUID.randomUUID(),
    val dataPointType: String,
    @Column(name = "data_point_id")
    val dataPointId: String,
    /**
     * QA reports are independently owned resources managed via [DataPointQaReportEntity]'s dedicated
     * API (see DataPointQaReportManager). This is a read-only association used to display the latest
     * QA reports for a data point within a judgement; it must not cascade any persistence operation
     * (in particular removal) onto the referenced QA report rows, since they must survive independently
     * of any dataset judgement's lifecycle.
     */
    @OneToMany
    @JoinColumn(name = "data_point_id", referencedColumnName = "data_point_id", insertable = false, updatable = false)
    val qaReports: MutableList<DataPointQaReportEntity> = mutableListOf(),
    var acceptedSource: AcceptedDataPointSource?,
    var reporterUserIdOfAcceptedQaReport: UUID?,
    @Column(columnDefinition = "TEXT", nullable = true)
    var customValue: String?,
    @Column(columnDefinition = "TEXT", nullable = true)
    var reasonForCustomDataPoint: String? = null,
    @JoinColumn(name = "dataset_judgement_id")
    @ManyToOne
    var datasetJudgement: DatasetJudgementEntity? = null,
    @Column(columnDefinition = "TEXT", nullable = true)
    @Convert(converter = PreApprovalCheckResultsConverter::class)
    var preApprovalCheckResults: PreApprovalCheckResults? = null,
) {
    /**
     * Converts this entity to its API response DTO.
     */
    fun toDataPointJudgementDetails(): DataPointJudgement =
        DataPointJudgement(
            dataPointType = dataPointType,
            dataPointId = dataPointId,
            qaReports = qaReports.map { it.toApiModel() },
            acceptedSource = acceptedSource,
            reporterUserIdOfAcceptedQaReport = reporterUserIdOfAcceptedQaReport,
            customValue = customValue,
            reasonForCustomDataPoint = reasonForCustomDataPoint,
            preApprovalCheckResults = preApprovalCheckResults,
        )
}
