package org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.interfaces.ApiModelConversion
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReportMetaInformation
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReportWithMetaInformation
import org.dataland.keycloakAdapter.auth.DatalandAuthentication

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
) : ApiModelConversion<QaReportMetaInformation> {

    /**
     * Method to convert the QA report entity to a model containing
     * the report and its meta information
     * @param objectMapper the object mapper to use for deserialization
     * @param clazz the class of the report to deserialize
     * @param viewingUser the user viewing the report
     */
    fun <ReportType> toFullApiModel(
        objectMapper: ObjectMapper,
        clazz: Class<ReportType>,
        viewingUser: DatalandAuthentication?,
    ): QaReportWithMetaInformation<ReportType> {
        val report = objectMapper.readValue(qaReport, clazz)
        return QaReportWithMetaInformation(
            metaInfo = toApiModel(viewingUser),
            report = report,
        )
    }

    override fun toApiModel(viewingUser: DatalandAuthentication?): QaReportMetaInformation {
        return QaReportMetaInformation(
            dataId = dataId,
            qaReportId = qaReportId,
            reporterUserId = reporterUserId,
            uploadTime = uploadTime,
            dataType = dataType,
            active = active,
        )
    }
}
