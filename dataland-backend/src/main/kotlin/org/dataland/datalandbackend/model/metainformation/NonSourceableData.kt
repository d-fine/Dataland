package org.dataland.datalandbackend.model.metainformation

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.DataType

/**
 * --- API model ---
 * Meta information storing the history on the if a data set is sourceable or not
 * * @param eventId unique identifier to identify the data in the data store
 * @param companyId unique identifier to identify the company the data is associated with
 * @param dataType type of the data
 * @param reportingPeriod marks a period - e.g. a year or a specific quarter in a year - for which the data is valid
 * @param nonSourceable true if the data set is non-sourceable
 * @param reason reason why the data set is non-sourceable
 * @param creationTime is a timestamp for the creation of this event
 */
data class NonSourceableData(
    @field:JsonProperty(required = true)
    val eventId: String,
    @field:JsonProperty(required = true)
    val companyId: String,
    @field:JsonProperty(required = true)
    val dataType: DataType,
    val reportingPeriod: String,
    @field:JsonProperty(required = true)
    val nonSourceable: Boolean,
    @field:JsonProperty(required = true)
    val reason: String,
    @field:JsonProperty(required = true)
    val creationTime: Long,
)
