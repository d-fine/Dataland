package org.dataland.datalandbackend.model.datapoints

import org.dataland.datalandbackend.interfaces.datapoints.DataPointDimensions
import org.dataland.datalandbackend.model.StorableDataSet
import java.util.UUID

/**
 * --- Non-API model ---
 * Class for defining the fields needed by the Data Manager to handle data storage
 * @param dataPointContent the content of the data point as a JSON string
 * @param dataPointIdentifier which data point the provided content is associated to
 * @param companyId identifies the company for which the data point is provided
 * @param reportingPeriod marks a period - e.g. a year or a specific quarter in a year - for which the data is valid
 */

data class UploadableDataPoint(
    val dataPointContent: String,
    override val dataPointIdentifier: String,
    override val companyId: UUID,
    override val reportingPeriod: String,
) : DataPointDimensions {
    /**
     * Converts the UploadableDataPoint to a StorableDataSet
     * @param uploaderUserId the user id of the user who uploaded the data point
     * @param uploadTime the time at which the data point was uploaded
     * @return a StorableDataSet object
     */
    fun toStorableDataSet(
        uploaderUserId: String,
        uploadTime: Long,
    ): StorableDataSet =
        StorableDataSet(
            companyId = companyId.toString(),
            dataType = dataPointIdentifier,
            uploaderUserId = uploaderUserId,
            uploadTime = uploadTime,
            reportingPeriod = reportingPeriod,
            data = dataPointContent,
        )
}
