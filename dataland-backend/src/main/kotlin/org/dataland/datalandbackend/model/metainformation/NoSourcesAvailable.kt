package org.dataland.datalandbackend.model.metainformation

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.DataType
import java.util.UUID

/**
 * --- API model ---
 * Meta information storing the history on datasets where no sources are avalaible
 * @param eventId unique identifier to identify the data in the data store
 * @param companyId unique identifier to identify the company the data is associated with
 * @param dataType type of the data
 * @param reportingPeriod marks a period - e.g. a year or a specific quarter in a year - for which the data is valid
 * @param noSourcesAvailable true if there is no source available
 * @param reason reason why there is no source available
 * @param creationTime is a timestamp for the creation of this event
 */
data class NoSourcesAvailable(
    @field:JsonProperty(required = true)
    val eventId: UUID,
    @field:JsonProperty(required = true)
    val companyId: String,
    @field:JsonProperty(required = true)
    val dataType: DataType,
    val reportingPeriod: String,
    @field:JsonProperty(required = true)
    val noSourcesAvailable: Boolean,
    @field:JsonProperty(required = true)
    val reason: String,
    @field:JsonProperty(required = true)
    val creationTime: Long,
)
