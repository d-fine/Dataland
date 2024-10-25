package org.dataland.datalandbackend.model.datapoints.standard

import java.time.LocalDate

/**
 * --- API model ---
 * Fields of a data point containing only a date
 */
data class LocalDateDataPoint(
    val value: LocalDate,
)
