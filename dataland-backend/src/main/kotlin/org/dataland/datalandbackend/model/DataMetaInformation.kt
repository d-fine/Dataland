package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * Meta information associated to data in the data store
 * @param dataId unique identifier to identify the data in the data store
 * @param dataType type of the data
 * @param companyId unique identifier to identify the company the data is associated with
 */
data class DataMetaInformation(
    @field:JsonProperty(required = true)
    val dataId: String,

    @field:JsonProperty(required = true)
    val dataType: DataType,

    val uploaderUserId: String? = null,

    @field:JsonProperty(required = true)
    val uploadTime: Long,

    @field:JsonProperty(required = true)
    val companyId: String,

    @field:JsonProperty(required = true)
    val isQualityAssured: String
)
