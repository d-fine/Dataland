package org.dataland.datalandbackend.model.datapoints

import org.dataland.datalandbackend.model.enums.data.QualityOptions
import java.util.UUID

/**
 * --- API model ---
 * Fields of a generic data point version 2
 */
data class DataPointV2<T, K>(
    val dataPointId: UUID,
    val value: T?,
    val dataSource: K?,
    val quality: QualityOptions?,
    val dataPointTypeId: UUID,
    val reportingPeriod: String,
    val companyId: UUID,
    val applicable: Boolean?,
)

data class GeneralDataPointV2<T, K>(
    val value: T?,
    val applicable: Boolean?,
    val dataSource: K?,
    val quality: QualityOptions?,
)

/*
    val test = "java.math.BigDecimal"
    val kotlinClass = Class.forName(test).kotlin
 */
