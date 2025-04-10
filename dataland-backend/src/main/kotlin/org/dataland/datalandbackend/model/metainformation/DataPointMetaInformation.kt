package org.dataland.datalandbackend.model.metainformation

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackendutils.interfaces.DataPointDimensions
import org.dataland.datalandbackendutils.model.QaStatus

/**
 * --- API model ---
 * Meta information associated to data in the data store
 * @param dataPointId unique identifier to identify the data in the data store
 * @param dataPointType unique identifier for the data point itself
 * @param companyId unique identifier to identify the company the data is associated with
 * @param reportingPeriod marks a period - e.g. a year or a specific quarter in a year - for which the data is valid
 * @param uploaderUserId the user ID of the user who uploaded the data point
 * @param uploadTime is a timestamp for the upload of this data point
 */
data class DataPointMetaInformation(
    @field:JsonProperty(required = true)
    val dataPointId: String,
    @field:JsonProperty(required = true)
    override val dataPointType: String,
    @field:JsonProperty(required = true)
    override val companyId: String,
    @field:JsonProperty(required = true)
    override val reportingPeriod: String,
    val uploaderUserId: String,
    @field:JsonProperty(required = true)
    val uploadTime: Long,
    val currentlyActive: Boolean,
    val qaStatus: QaStatus,
) : DataPointDimensions
