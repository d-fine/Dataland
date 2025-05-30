package org.dataland.datalandbackend.model.metainformation

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.DataType

/**
 * --- API model ---
 * SourceabilityInfoResponse is used for api response (get requests). Returns information regarding whether data for a
 * triple (companyId, dataType, reportingPeriod) is available.
 * @param companyId unique identifier to identify the company the data is associated with
 * @param dataType type of the data
 * @param reportingPeriod marks a period - e.g. a year or a specific quarter in a year - for which the data is valid
 * @param isNonSourceable true if there is no source available
 * @param reason reason why there is no source available
 * @param userId user who uploaded information on the sourceability of the date set
 * @param creationTime time when the info has been posted
 */
data class SourceabilityInfoResponse(
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
    @field:JsonProperty(required = true)
    val creationTime: Long,
    @field:JsonProperty(required = true)
    val userId: String,
)
