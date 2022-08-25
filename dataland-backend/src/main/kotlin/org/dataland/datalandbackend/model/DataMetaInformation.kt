package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.hibernate.annotations.Immutable
import javax.persistence.*

/**
 * --- API model ---
 * Meta information associated to data in the data store
 * @param dataId unique identifier to identify the data in the data store
 * @param dataType type of the data
 * @param companyId unique identifier to identify the company the data is associated with
 */
@Entity
@Immutable
@Table(name = "data_meta_information")
data class DataMetaInformation(
    @Id
    @Column(name = "data_id")
    @field:JsonProperty(required = true)
    val dataId: String,

    @Column(name = "data_type", nullable = false)
    @Convert(converter = DataTypeJpaConverter::class)
    @field:JsonProperty(required = true)
    val dataType: DataType,

    @Column(name = "company_id", nullable = false)
    @field:JsonProperty(required = true)
    val companyId: String
)
