package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty

data class DataSetMetaInformation(
    @field:JsonProperty("name", required = true) val name: String,
    @field:JsonProperty("id", required = true) val id: String
)
