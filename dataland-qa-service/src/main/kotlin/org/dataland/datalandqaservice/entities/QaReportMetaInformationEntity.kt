package org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReportMetaInformation
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.interfaces.ApiModelConversion
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import java.util.*

/**
 * The database entity for storing metadata regarding QA reports uploaded to dataland
 */
@Entity
@Table(
    name = "qa_report_meta_information",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["data_id", "reporter_user_id", "upload_time"])
    ],
)
data class QaReportMetaInformationEntity(
    @Id
    @Column(name = "qa_report_id")
    val qaReportId: UUID,

    //ToDO: or maybe instead
    /*
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_id")
    var data: DataMetaInformationEntity,
    */
    @Column(name = "data_id", nullable = false)
    var dataId: UUID,

    @Column(name = "reporter_user_id", nullable = false)
    var reporterUserId: UUID,

    @Column(name = "upload_time", nullable = false)
    var uploadTime: Long
) : ApiModelConversion<QaReportMetaInformation> {

    override fun toApiModel(viewingUser: DatalandAuthentication?): QaReportMetaInformation {
        return QaReportMetaInformation(
            dataId = dataId,
            qaReportId = qaReportId,
            reporterUserId = reporterUserId,
            uploadTime = uploadTime
        )
    }
}