package org.dataland.datalandbackend.model.metainformation

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.DataType

/**
 * --- API model ---
 * NonSourceableInfo storing the history of whether a dataset is sourceable or not used for posting and for message queue.
 * @param companyId unique identifier to identify the company the data is associated with
 * @param dataType type of the data
 * @param reportingPeriod marks a period - e.g. a year or a specific quarter in a year - for which the data is valid
 * @param isNonSourceable true if there is no source available
 * @param reason reason why there is no source available
 */
data class NonSourceableInfo(
    @field:JsonProperty(required = true)
    val companyId: String,
    @field:JsonProperty(required = true)
    val dataType: DataType,
    @field:JsonProperty(required = true)
    val reportingPeriod: String,
    @field:JsonProperty(required = true)
    val isNonSourceable: Boolean,
    @field:JsonProperty(required = true)
    val reason: String,
)
