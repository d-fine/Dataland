package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Class for defining the fields needed by the Data Manager to handle data storage
 * @param companyId identifies the company for which a data set is to be stored
 * @param dataType the type of the data set
 * @param data the actual data
 */
data class StoredDataSet(
    @field:JsonProperty(required = true) val companyId: String,
    @field:JsonProperty(required = true) val dataType: String,
    @field:JsonProperty(required = true) val data: String
)
