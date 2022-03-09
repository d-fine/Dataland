package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Class for defining the content of a data set
 * @param name identifies the data set
 * @param payload content of the stored data
 */
data class DataSet(
    @field:JsonProperty("name", required = true) val name: String,
    @field:JsonProperty("payload", required = true) val payload: String
)
