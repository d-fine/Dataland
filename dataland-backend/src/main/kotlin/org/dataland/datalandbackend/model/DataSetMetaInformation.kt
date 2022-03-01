package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Meta information associated to a data set in the data store
 * @param name name of the data set
 * @param id unique identifier to identify the data set in the data store
 */
data class DataSetMetaInformation(
    @field:JsonProperty("name", required = true) val name: String,
    @field:JsonProperty("id", required = true) val id: String
)
