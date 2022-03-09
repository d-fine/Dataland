package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Class for defining the content of a data set
 * @param name identifies the data set
 * @param payload content of the stored data
 */
data class StoredDataSet(
    @field:JsonProperty(required = true) val companyId: String,
    @field:JsonProperty(required = true) val dataIdentifier: DataIdentifier,
    @field:JsonProperty(required = true) val data: String
)
