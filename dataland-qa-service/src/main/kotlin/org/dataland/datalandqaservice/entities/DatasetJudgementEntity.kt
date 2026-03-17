package org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.converters.DatasetJudgementStateConverter
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetJudgementResponse
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetJudgementState
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReporter
import java.util.UUID

/**
 * The database entity for storing dataset reviews
 */
@Suppress("LongParameterList")
@Entity
@Table(name = "dataset_judgement")
class DatasetJudgementEntity(
    @Id
    @Column(name = "dataset_judgement_id")
    val dataSetJudgementId: UUID,
    @Column(name = "dataset_id")
    val datasetId: UUID,
    @Column(name = "company_id")
    val companyId: UUID,
    @Column(name = "data_type")
    val dataType: String,
    @Column(name = "reporting_period")
    val reportingPeriod: String,
    @Column(name = "judgement_state")
    @Convert(converter = DatasetJudgementStateConverter::class)
    var judgementState: DatasetJudgementState = DatasetJudgementState.Pending,
    @Column(name = "judge_user_id")
    var qaJudgeUserId: UUID,
    @Column(name = "judge_user_name")
    var qaJudgeUserName: String,
    @ElementCollection
    @Column(name = "qa_reporters")
    var qaReporters: MutableList<QaReporter>,
    @OneToMany(mappedBy = "datasetJudgement", cascade = [CascadeType.ALL])
    val dataPoints: MutableList<DataPointJudgementEntity>,
) {
    /**
     * Convert to DatasetJudgement objects for API use.
     */
    fun toDatasetJudgementResponse(): DatasetJudgementResponse =
        DatasetJudgementResponse(
            dataSetJudgementId.toString(),
            datasetId.toString(),
            companyId.toString(),
            dataType,
            reportingPeriod,
            judgementState,
            qaJudgeUserId.toString(),
            qaJudgeUserName,
            qaReporters.toList(),
            dataPoints.associateBy({ it.dataPointType }, { it.toDataPointJudgementDetails() }),
        )

    /**
     * Add an associated request to this data sourcing entity.
     * Make sure the data sourcing entity is also added to the request.
     */
    fun addAssociatedDataPoints(dataPoint: DataPointJudgementEntity) {
        dataPoints.add(dataPoint)
        dataPoint.datasetJudgement = this
    }
}
