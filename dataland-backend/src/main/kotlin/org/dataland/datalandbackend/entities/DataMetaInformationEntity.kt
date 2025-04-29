package org.dataland.datalandbackend.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.dataland.datalandbackend.interfaces.ApiModelConversion
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
import org.dataland.datalandbackendutils.model.BasicDataDimensions
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole

/**
 * The database entity for storing metadata regarding data uploaded to dataland
 */
@Entity
@Table(
    name = "data_meta_information",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["company_id", "data_type", "reporting_period", "currently_active"]),
        UniqueConstraint(columnNames = ["company_id", "data_type", "reporting_period", "upload_time"]),
    ],
)
data class DataMetaInformationEntity(
    @Id
    @Column(name = "data_id")
    val dataId: String,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    var company: StoredCompanyEntity,
    @Column(name = "data_type", nullable = false)
    var dataType: String,
    @Column(name = "uploader_user_id", nullable = false)
    var uploaderUserId: String,
    @Column(name = "upload_time", nullable = false)
    var uploadTime: Long,
    @Column(name = "reporting_period", nullable = false)
    var reportingPeriod: String,
    @Column(name = "currently_active", nullable = true)
    var currentlyActive: Boolean?,
    @Column(name = "quality_status", nullable = false)
    var qaStatus: QaStatus,
) : ApiModelConversion<DataMetaInformation> {
    /**
     * The viewingUser can view information about the dataset or the dataset itself if
     * (a) the dataset is QAd
     * (b) the user has uploaded the dataset
     * (c) the user is an admin or a reviewer
     * This function checks these conditions.
     */
    fun isDatasetViewableByUser(viewingUser: DatalandAuthentication?): Boolean =
        this.qaStatus == QaStatus.Accepted ||
            this.uploaderUserId == viewingUser?.userId ||
            isDatasetViewableByUserViaRole(viewingUser?.roles ?: emptySet())

    private fun isDatasetViewableByUserViaRole(roles: Set<DatalandRealmRole>): Boolean =
        roles.contains(DatalandRealmRole.ROLE_ADMIN) || roles.contains(DatalandRealmRole.ROLE_REVIEWER)

    override fun toApiModel(): DataMetaInformation =
        DataMetaInformation(
            dataId = dataId,
            companyId = company.companyId,
            dataType = DataType.valueOf(dataType),
            uploaderUserId = uploaderUserId,
            uploadTime = uploadTime,
            reportingPeriod = reportingPeriod,
            currentlyActive = currentlyActive == true,
            qaStatus = qaStatus,
        )

    /**
     * Converts this entity to the DataMetaInformation API model and sets the URL to the proxy primary URL
     * @param proxyPrimaryUrl the proxy primary URL to set
     */
    fun toApiModel(proxyPrimaryUrl: String): DataMetaInformation {
        val dataMetaInformation = this.toApiModel()
        dataMetaInformation.ref = "https://$proxyPrimaryUrl/companies/${company.companyId}/frameworks/$dataType/$dataId"
        return dataMetaInformation
    }

    /**
     * Converts the entity into the basic data dimension object
     * return a BasicDataDimensions object
     */
    fun toBasicDataDimensions(): BasicDataDimensions =
        BasicDataDimensions(
            companyId = company.companyId,
            dataType = dataType,
            reportingPeriod = reportingPeriod,
        )
}
