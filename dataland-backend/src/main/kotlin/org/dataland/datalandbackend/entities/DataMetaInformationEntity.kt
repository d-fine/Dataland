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
import org.dataland.datalandbackend.model.DataMetaInformation
import org.dataland.datalandbackend.model.DataType
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
) : ApiModelConversion<DataMetaInformation> {

    override fun toApiModel(viewingUser: DatalandAuthentication?): DataMetaInformation {
        val displayUploaderUserId = viewingUser != null && (
            viewingUser.roles.contains(DatalandRealmRole.ROLE_ADMIN) ||
                viewingUser.userId == uploaderUserId
            )
        return DataMetaInformation(
            dataId = dataId,
            companyId = company.companyId,
            dataType = DataType.valueOf(dataType),
            uploaderUserId = if (displayUploaderUserId) uploaderUserId else null,
            uploadTime = uploadTime,
            reportingPeriod = reportingPeriod,
            currentlyActive = currentlyActive == true,
        )
    }
}
