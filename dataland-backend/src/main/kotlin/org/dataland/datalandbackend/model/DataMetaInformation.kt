package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

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

    @field:JsonProperty()
    val uploaderUserId: String?,

    @field:JsonProperty()
    val uploadTime: Instant,

    @field:JsonProperty(required = true)
    val companyId: String
)
