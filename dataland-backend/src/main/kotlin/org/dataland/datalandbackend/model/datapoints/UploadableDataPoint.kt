package org.dataland.datalandbackend.model.datapoints

import java.util.UUID

/**
 * --- Non-API model ---
 * Class for defining the fields needed by the Data Manager to handle data storage
 * @param data the content of the data point as a JSON string
 * @param dataPoint the data point provided
 * @param companyId identifies the company for which the data point is provided
 * @param reportingPeriod marks a period - e.g. a year or a specific quarter in a year - for which the data is valid
 */

data class UploadableDataPoint(
    // ToDo: introduce interface for triple and use in the related types
    val data: String,
    val dataPoint: String,
    val companyId: UUID,
    val reportingPeriod: String,
)
