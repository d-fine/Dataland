package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Meta information associated to a data set in the data store
 * @param dataType type of a specific data set
 * @param companyId unique identifier to identify the company the data set belongs to
 */
data class DataSetMetaInformation(
    @field:JsonProperty(required = true) val dataType: String,
    @field:JsonProperty(required = true) val companyId: String
)
