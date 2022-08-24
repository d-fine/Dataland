package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

/**
 * --- API model ---
 * Meta information associated to data in the data store
 * @param dataId unique identifier to identify the data in the data store
 * @param dataType type of the data
 * @param companyId unique identifier to identify the company the data is associated with
 */
@Entity
@Table(name = "data_meta_information")
data class DataMetaInformation(
    @Id
    @Column(name = "data_id")
    @field:JsonProperty(required = true)
    val dataId: String,

    @Column(name = "data_type", nullable = false)
    @field:JsonProperty(required = true)
    val dataType: DataType,

    @Column(name = "company_id", nullable = false)
    // @ForeignKey()
    @field:JsonProperty(required = true)
    val companyId: String
)
