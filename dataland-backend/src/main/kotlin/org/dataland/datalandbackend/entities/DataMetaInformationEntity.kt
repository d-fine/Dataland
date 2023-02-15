package org.dataland.datalandbackend.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.dataland.datalandbackend.interfaces.ApiModelConversion
import org.dataland.datalandbackend.model.DataMetaInformation
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.enums.data.DatasetQualityStatus
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole

/**
 * The database entity for storing metadata regarding data uploaded to dataland
 */
@Entity
@Table(name = "data_meta_information")
data class DataMetaInformationEntity(
    @Id
    @Column(name = "data_id")
    val dataId: String,

    @Column(name = "data_type", nullable = false)
    var dataType: String,

    @Column(name = "uploader_user_id", nullable = false)
    var uploaderUserId: String,

    @Column(name = "upload_time", nullable = false)
    var uploadTime: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    var company: StoredCompanyEntity,

    @Column(name = "quality_status", nullable = false)
    var qualityStatus: DatasetQualityStatus,
) : ApiModelConversion<DataMetaInformation> {

    override fun toApiModel(viewingUser: DatalandAuthentication?): DataMetaInformation {
        val displayUploaderUserId = viewingUser != null && (
            viewingUser.roles.contains(DatalandRealmRole.ROLE_ADMIN) ||
                viewingUser.userId == this.uploaderUserId
            )
        return DataMetaInformation(
            dataId = dataId,
            dataType = DataType.valueOf(dataType),
            uploaderUserId = if (displayUploaderUserId) this.uploaderUserId else null,
            uploadTime = this.uploadTime,
            companyId = company.companyId,
            qualityStatus = qualityStatus,
        )
    }
}
