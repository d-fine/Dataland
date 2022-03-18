package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Data identifier for a data set containing also data type info
 * @param dataId unique identifier to identify the data set in the data store
 * @param dataType type of the data set
 */
data class DataIdentifier(
    @field:JsonProperty("Data ID", required = true) val dataId: String,
    @field:JsonProperty("Data Type", required = true) val dataType: String
)
