package org.dataland.datalandbackend.model.datapoints

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * Class defining the content of a body for data point validation
 * @param dataPoint the data point for validation as a JSON string
 * @param dataPointType which data point type the provided data is supposedly associated to
 */
data class DataPointToValidate(
    @field:JsonProperty(required = true)
    val dataPoint: String,
    @field:JsonProperty(required = true)
    val dataPointType: String,
)
