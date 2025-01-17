package org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.dataland.datalandqaservice.model.reports.QaReportDataPoint
import org.dataland.datalandqaservice.model.reports.QaReportDataPointVerdict
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.converters.QaReportDataPointVerdictConverter
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.DataPointQaReport

/**
 * The database entity for storing metadata regarding QA reports uploaded to dataland
 */
@Entity
@Table(name = "data_point_qa_reports")
data class DataPointQaReportEntity(
    @Id
    @Column(name = "qa_report_id")
    val qaReportId: String,
    @Column(name = "comment", columnDefinition = "TEXT", nullable = false)
    var comment: String,
    @Convert(converter = QaReportDataPointVerdictConverter::class)
    @Column(name = "verdict", nullable = false)
    var verdict: QaReportDataPointVerdict,
    @Column(name = "corrected_data", columnDefinition = "TEXT", nullable = true)
    var correctedData: String?,
    @Column(name = "data_point_id", nullable = false)
    var dataPointId: String,
    @Column(name = "data_point_type", nullable = false)
    var dataPointType: String,
    @Column(name = "reporter_user_id", nullable = false)
    var reporterUserId: String,
    @Column(name = "upload_time", nullable = false)
    var uploadTime: Long,
    @Column(name = "active", nullable = false)
    var active: Boolean,
) {
    /**
     * Method to convert the QA report entity to a model containing only the meta information of the report
     */
    fun toApiModel(): DataPointQaReport =
        DataPointQaReport(
            dataPointId = dataPointId,
            qaReportId = qaReportId,
            reporterUserId = reporterUserId,
            uploadTime = uploadTime,
            dataPointType = dataPointType,
            active = active,
            comment = comment,
            verdict = verdict,
            correctedData = correctedData,
        )

    /**
     * Method to convert the QA report entity to a model containing the meta information and the corrected data of the report
     */
    fun toDatasetApiModel(objectMapper: ObjectMapper): QaReportDataPoint<JsonNode> =
        QaReportDataPoint(
            comment = comment,
            verdict = verdict,
            correctedData = objectMapper.readTree(correctedData),
        )
}
