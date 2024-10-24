package org.dataland.datalandbackend.model.datapoints

/**
 * --- API model ---
 * Fields of a generic data point
 */
data class GenericDataPoint<T>(
    val value: T?,
    val applicable: Boolean?,
    )
