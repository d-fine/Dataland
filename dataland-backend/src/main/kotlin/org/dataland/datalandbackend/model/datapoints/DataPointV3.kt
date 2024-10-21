package org.dataland.datalandbackend.model.datapoints

import org.dataland.datalandbackend.model.enums.data.QualityOptions
import java.util.UUID

/**
 * --- API model ---
 * Fields of a generic data point version 3
 */
data class DataPointV3<T>(
    val dataPointId: UUID,
    val value: T?,
    val dataPointTypeId: UUID,
    val reportingPeriod: String,
    val companyId: UUID,
    val applicable: Boolean?,
)

data class DataSource<T>(
    val dataPointId: UUID,
    val dataSource: T,
    val quality: QualityOptions?,
)
