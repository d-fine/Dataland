package org.dataland.datalandbackend.model.datapoints

import java.util.UUID

/**
 * --- API model ---
 * Fields of a currency data point without restrictions on the value
 */
data class StoredDataPointNew(
    val dataPointId: UUID,
    val data: String,
    val companyId: UUID,
    val reportingPeriod: String,
    val dataPointType: String,
)
