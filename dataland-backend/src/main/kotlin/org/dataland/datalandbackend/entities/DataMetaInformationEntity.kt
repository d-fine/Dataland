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
import org.dataland.keycloakAdapter.DatalandRealmRoles
import org.dataland.keycloakAdapter.utils.getUserId
import org.dataland.keycloakAdapter.utils.hasRole
import org.springframework.security.core.Authentication

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
) : ApiModelConversion<DataMetaInformation> {

    override fun toApiModel(): DataMetaInformation {
        return DataMetaInformation(
            dataId = dataId,
            dataType = DataType.valueOf(dataType),
            uploaderUserId = null,
            uploadTime = this.uploadTime,
            companyId = company.companyId,
        )
    }

    /**
     * Returns the API model to be shown to the given viewingUser
     * The uploaderUserId field will be populated if the user is admin or the uploader of this data
     * otherwise the field will remain empty
     */
    fun toApiModel(viewingUser: Authentication): DataMetaInformation {
        val displayUploaderUserId = viewingUser.hasRole(DatalandRealmRoles.ROLE_ADMIN) ||
            viewingUser.getUserId() == this.uploaderUserId

        return if (displayUploaderUserId) toApiModel().copy(uploaderUserId = this.uploaderUserId)
        else toApiModel()
    }
}
