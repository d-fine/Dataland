package org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DataPointJudgement
import java.util.UUID

/**
 * JPA entity storing per-data-point information within a dataset judgement.
 */
@Suppress("LongParameterList")
@Entity
@Table(name = "dataset_judgement_entity_data_point_judgement_details")
class DataPointJudgementEntity(
    @Id val id: UUID = UUID.randomUUID(),
    val dataPointType: String,
    @Column(name = "data_point_id")
    val dataPointId: String,
    @OneToMany(cascade = [CascadeType.ALL])
    @JoinColumn(name = "data_point_id", referencedColumnName = "data_point_id")
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
    fun toDataPointJudgementDetails(): DataPointJudgement =
        DataPointJudgement(
            dataPointType = dataPointType,
            dataPointId = dataPointId,
            qaReports = qaReports.map { it.toApiModel() },
            acceptedSource = acceptedSource,
            reporterUserIdOfAcceptedQaReport = reporterUserIdOfAcceptedQaReport,
            customValue = customValue,
        )
}
