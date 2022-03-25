package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Required input for data manager to get valid data from data store
 * @param dataId unique identifier to identify data in the data store
 * @param dataType expected type of the data
 */
data class DataManagerInputToGetData(
    @field:JsonProperty("Data ID", required = true) val dataId: String,
    @field:JsonProperty("Data Type", required = true) val dataType: String
)
