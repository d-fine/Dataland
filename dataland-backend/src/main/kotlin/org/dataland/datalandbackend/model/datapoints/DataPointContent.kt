package org.dataland.datalandbackend.model.datapoints

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * Class defining the content of a datapoint for validation
 * @param dataPointContent the content of the data point as a JSON string
 * @param dataPointIdentifier which data point the provided content is associated to
 */
data class DataPointContent(
    @field:JsonProperty(required = true)
    val dataPointContent: String,
    @field:JsonProperty(required = true)
    val dataPointIdentifier: String,
)
