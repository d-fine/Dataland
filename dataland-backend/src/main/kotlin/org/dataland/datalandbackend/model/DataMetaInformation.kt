package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackendutils.model.QAStatus

/**
 * --- API model ---
 * Meta information associated to data in the data store
 * @param dataId unique identifier to identify the data in the data store
 * @param companyId unique identifier to identify the company the data is associated with
 * @param dataType type of the data
 * @param uploaderUserId the user ID of the user who requested the upload of this dataset
 * @param uploadTime is a timestamp for the upload of this dataset
 * @param reportingPeriod marks a period - e.g. a year or a specific quarter in a year - for which the data is valid
 */
data class DataMetaInformation(
    @field:JsonProperty(required = true)
    val dataId: String,

    @field:JsonProperty(required = true)
    val companyId: String,

    @field:JsonProperty(required = true)
    val dataType: DataType,

    val uploaderUserId: String? = null,

    @field:JsonProperty(required = true)
    val uploadTime: Long,

    @field:JsonProperty(required = true)
    val reportingPeriod: String,

    @field:JsonProperty(required = true)
    val currentlyActive: Boolean,

    @field:JsonProperty(required = true)
    var qaStatus: QAStatus,
)
