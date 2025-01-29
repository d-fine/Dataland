package org.dataland.datalandbackend.model.metainformation

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackendutils.model.QaStatus

/**
 * --- API model ---
 * Meta information associated to data in the data store
 * @param dataId unique identifier to identify the data in the data store
 * @param companyId unique identifier to identify the company the data is associated with
 * @param dataType type of the data
 * @param uploaderUserId the user ID of the user who requested the upload of this dataset
 * @param uploadTime is a timestamp for the upload of this dataset
 * @param reportingPeriod marks a period - e.g. a year or a specific quarter in a year - for which the data is valid
 * @param url direct link to the page displaying the specified data set
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
    var qaStatus: QaStatus,
    @field:JsonProperty(required = true)
    val url: String,
) {
    companion object {
        @Suppress("MayBeConst")
        private val proxyPrimaryUrl: String = "\${dataland.backend.proxy-primary-url}"
    }

    constructor(
        dataId: String,
        companyId: String,
        dataType: DataType,
        uploaderUserId: String? = null,
        uploadTime: Long,
        reportingPeriod: String,
        currentlyActive: Boolean,
        qaStatus: QaStatus,
    ) : this(
        dataId = dataId,
        companyId = companyId,
        dataType = dataType,
        uploaderUserId = uploaderUserId,
        uploadTime = uploadTime,
        reportingPeriod = reportingPeriod,
        currentlyActive = currentlyActive,
        qaStatus = qaStatus,
        url = "https://$proxyPrimaryUrl/companies/$companyId/frameworks/$dataId",
    )
}
