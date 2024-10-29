package org.dataland.datalandbackend.model.datapoints

import java.util.UUID

/**
 * --- Non-API model ---
 * Class for defining the fields needed by the Data Manager to handle data storage
 * @param dataPointId identifies the data point
 * @param datapointSpecification the type of data point
 * @param companyId identifies the company for which the data point is provided
 * @param reportingPeriod marks a period - e.g. a year or a specific quarter in a year - for which the data is valid
 * @param uploaderUserId identifies the user who uploaded the data point
 * @param uploadTime the time at which the data point was uploaded
 * @param currentlyActive indicates whether the data point represents the latest data or not
 */

data class DataPointMetaData(
    val dataPointId: UUID,
    // TODO: rename to datapointSpecification
    val datapointSpecification: String,
    val companyId: UUID,
    val reportingPeriod: String,
    val uploaderUserId: UUID,
    val uploadTime: Long,
    val currentlyActive: Boolean,
)
