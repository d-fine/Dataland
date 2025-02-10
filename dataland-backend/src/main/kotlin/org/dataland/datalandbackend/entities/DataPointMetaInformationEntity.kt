package org.dataland.datalandbackend.entities

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.dataland.datalandbackend.interfaces.ApiModelConversion
import org.dataland.datalandbackend.model.metainformation.DataPointMetaInformation
import org.dataland.datalandbackendutils.converter.QaStatusConverter
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole

/**
 * The database entity for storing metadata regarding data points uploaded to dataland
 */
@Entity
@Table(
    name = "data_point_meta_information",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["company_id", "data_point_type", "reporting_period", "currently_active"]),
        UniqueConstraint(columnNames = ["company_id", "data_point_type", "reporting_period", "upload_time"]),
    ],
)
data class DataPointMetaInformationEntity(
    @Id
    @Column(name = "data_point_id")
    val dataPointId: String,
    @Column(name = "companyId", nullable = false)
    var companyId: String,
    @Column(name = "data_point_type", nullable = false)
    var dataPointType: String,
    @Column(name = "reporting_period", nullable = false)
    var reportingPeriod: String,
    @Column(name = "uploader_user_id", nullable = false)
    var uploaderUserId: String,
    @Column(name = "upload_time", nullable = false)
    var uploadTime: Long,
    @Column(name = "currently_active", nullable = true)
    var currentlyActive: Boolean?,
    @Column(name = "quality_status", nullable = false)
    @Convert(converter = QaStatusConverter::class)
    var qaStatus: QaStatus,
) : ApiModelConversion<DataPointMetaInformation> {
    /**
     * The viewingUser can view information about the data point or the data point itself if
     * (a) the data point has successfully passed QA
     * (b) the user has uploaded the data point
     * (c) the user is an admin or a reviewer
     * This function checks these conditions.
     */
    fun isDatasetViewableByUser(viewingUser: DatalandAuthentication?): Boolean =
        this.qaStatus == QaStatus.Accepted ||
            this.uploaderUserId == viewingUser?.userId ||
            isDatasetViewableByUserViaRole(viewingUser?.roles ?: emptySet())

    private fun isDatasetViewableByUserViaRole(roles: Set<DatalandRealmRole>): Boolean =
        roles.contains(DatalandRealmRole.ROLE_ADMIN) || roles.contains(DatalandRealmRole.ROLE_REVIEWER)

    override fun toApiModel(): DataPointMetaInformation =
        DataPointMetaInformation(
            dataPointId = dataPointId,
            companyId = companyId,
            dataPointType = dataPointType,
            uploaderUserId = uploaderUserId,
            uploadTime = uploadTime,
            reportingPeriod = reportingPeriod,
            currentlyActive = currentlyActive == true,
            qaStatus = qaStatus,
        )
}
