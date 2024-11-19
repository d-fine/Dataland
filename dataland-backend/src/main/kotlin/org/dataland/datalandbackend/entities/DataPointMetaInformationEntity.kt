package org.dataland.datalandbackend.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.dataland.datalandbackend.interfaces.ApiModelConversion
import org.dataland.datalandbackend.model.metainformation.DataPointMetaInformation
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import java.util.UUID

/**
 * The database entity for storing metadata regarding data points uploaded to dataland
 */
@Entity
@Table(
    name = "data_point_meta_information",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["company_id", "data_point_identifier", "reporting_period", "currently_active"]),
        UniqueConstraint(columnNames = ["company_id", "data_point_identifier", "reporting_period", "upload_time"]),
    ],
)
data class DataPointMetaInformationEntity(
    @Id
    @Column(name = "data_id")
    val dataId: UUID,
    @Column(name = "companyId", nullable = false)
    var companyId: UUID,
    @Column(name = "data_point_identifier", nullable = false)
    var dataPointIdentifier: String,
    @Column(name = "reporting_period", nullable = false)
    var reportingPeriod: String,
    @Column(name = "uploader_user_id", nullable = false)
    var uploaderUserId: String,
    @Column(name = "upload_time", nullable = false)
    var uploadTime: Long,
    @Column(name = "currently_active", nullable = true)
    var currentlyActive: Boolean?,
    @Column(name = "quality_status", nullable = false)
    var qaStatus: QaStatus,
) : ApiModelConversion<DataPointMetaInformation> {
    override fun toApiModel(viewingUser: DatalandAuthentication?): DataPointMetaInformation =
        DataPointMetaInformation(
            dataId = dataId,
            companyId = companyId,
            dataPointIdentifier = dataPointIdentifier,
            uploaderUserId = uploaderUserId,
            uploadTime = uploadTime,
            reportingPeriod = reportingPeriod,
            currentlyActive = currentlyActive == true,
            qaStatus = qaStatus,
        )
}
