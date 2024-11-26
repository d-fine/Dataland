package org.dataland.datalandinternalstorage.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackendutils.interfaces.DataPointInstance

/**
 * --- API model ---
 * Class for defining the fields representing the data point in the data storage
 * @param dataPointContent the content of the data point as a JSON string
 * @param dataPointIdentifier which kind of data point the provided content is associated to
 * @param companyId identifies the company for which the data point is provided
 * @param reportingPeriod marks a period - e.g. a year or a specific quarter in a year - for which the data is valid
 */
data class StorableDataPoint(
    @field:JsonProperty(required = true)
    override val dataPointContent: String,
    @field:JsonProperty(required = true)
    override val dataPointIdentifier: String,
    @field:JsonProperty(required = true)
    override val companyId: String,
    @field:JsonProperty(required = true)
    override val reportingPeriod: String,
) : DataPointInstance
