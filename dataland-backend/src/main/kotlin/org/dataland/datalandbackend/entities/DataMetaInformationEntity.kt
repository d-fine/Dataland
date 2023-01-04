package org.dataland.datalandbackend.entities

import org.dataland.datalandbackend.interfaces.ApiModelConversion
import org.dataland.datalandbackend.model.DataMetaInformation
import org.dataland.datalandbackend.model.DataType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    var company: StoredCompanyEntity,
) : ApiModelConversion<DataMetaInformation> {

    override fun toApiModel(): DataMetaInformation {
        return DataMetaInformation(
            dataId = dataId,
            dataType = DataType.valueOf(dataType),
            companyId = company.companyId,
        )
    }
}
