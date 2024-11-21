package org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReportMetaInformation
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReportWithMetaInformation

/**
 * The database entity for storing metadata regarding QA reports uploaded to dataland
 */
@Entity
@Table(
    name = "qa_reports",
)
data class QaReportEntity(
    @Id
    @Column(name = "qa_report_id")
    val qaReportId: String,
    @Column(name = "qa_report", columnDefinition = "TEXT", nullable = false)
    var qaReport: String,
    @Column(name = "data_id", nullable = false)
    var dataId: String,
    @Column(name = "data_type", nullable = false)
    var dataType: String,
    @Column(name = "reporter_user_id", nullable = false)
    var reporterUserId: String,
    @Column(name = "upload_time", nullable = false)
    var uploadTime: Long,
    @Column(name = "active", nullable = false)
    var active: Boolean,
) {
    /**
     * Method to convert the QA report entity to a model containing the report and its meta information
     * @param objectMapper the object mapper to use for deserialization
     * @param clazz the class of the report to deserialize
     */
    fun <ReportType> toFullApiModel(
        objectMapper: ObjectMapper,
        clazz: Class<ReportType>,
    ): QaReportWithMetaInformation<ReportType> {
        val report = objectMapper.readValue(qaReport, clazz)
        return QaReportWithMetaInformation(
            metaInfo = toMetaInformationApiModel(),
            report = report,
        )
    }

    /**
     * Method to convert the QA report entity to a model containing only the meta information of the report
     */
    fun toMetaInformationApiModel(): QaReportMetaInformation =
        QaReportMetaInformation(
            dataId = dataId,
            qaReportId = qaReportId,
            reporterUserId = reporterUserId,
            uploadTime = uploadTime,
            dataType = dataType,
            active = active,
        )
}
