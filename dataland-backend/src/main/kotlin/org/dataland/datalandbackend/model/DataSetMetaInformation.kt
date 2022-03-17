package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Meta information associated to a data set in the data store
 * @param dataIdentifier identifier for a data set
 * @param companyId unique identifier to identify the company the data set belongs to
 */
data class DataSetMetaInformation(
    @field:JsonProperty(required = true) val dataIdentifier: DataIdentifier,
    @field:JsonProperty(required = true) val companyId: String
)
