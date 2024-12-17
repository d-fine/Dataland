package org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReportMetaInformation

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
