package org.dataland.datalandbackend.model.datapoints

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.entities.DataPointMetaInformationEntity
import org.dataland.datalandbackendutils.interfaces.DataPointInstance
import org.dataland.datalandbackendutils.model.QaStatus
import java.time.Instant

/**
 * --- API model ---
 * Class for defining the fields needed by the Data Point Manager to handle data storage
 * @param dataPointContent the content of the data point as a JSON string
 * @param dataPointIdentifier which data point the provided content is associated to
 * @param companyId identifies the company for which the data point is provided
 * @param reportingPeriod marks a period - e.g. a year or a specific quarter in a year - for which the data is valid
 */

data class UploadedDataPoint(
    @field:JsonProperty(required = true)
    override val dataPointContent: String,
    @field:JsonProperty(required = true)
    override val dataPointIdentifier: String,
    @field:JsonProperty(required = true)
    override val companyId: String,
    @field:JsonProperty(required = true)
    override val reportingPeriod: String,
) : DataPointInstance {
    /**
     * Method to convert the uploaded data point to a data point meta information entity
     * @param dataId the id of the data point
     * @param uploaderUserId the user id of the user who uploaded the data point
     * @param uploadTime the time of the upload (default is the current time)
     * @param currentlyActive whether the data point is currently active (default is false)
     * @param qaStatus the QA status of the data point (default is Pending)
     */
    fun toDataPointMetaInformationEntity(
        dataId: String,
        uploaderUserId: String,
        uploadTime: Long = Instant.now().toEpochMilli(),
        currentlyActive: Boolean? = null,
        qaStatus: QaStatus = QaStatus.Pending,
    ): DataPointMetaInformationEntity =
        DataPointMetaInformationEntity(
            dataId = dataId,
            companyId = companyId,
            dataPointIdentifier = dataPointIdentifier,
            reportingPeriod = reportingPeriod,
            uploaderUserId = uploaderUserId,
            uploadTime = uploadTime,
            currentlyActive = currentlyActive,
            qaStatus = qaStatus,
        )
}
