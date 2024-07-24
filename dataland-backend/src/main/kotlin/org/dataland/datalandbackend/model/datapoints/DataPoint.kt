package org.dataland.datalandbackend.model.datapoints

/**
 * --- API model ---
 * Fields of a generic data point
 */
data class DataPoint(
    val data: String,
    val dataPointId: String,
    val reportingPeriod: String,
    val companyId: String,
    val uploaderUserId: String,
    val uploadTime: Long
)
