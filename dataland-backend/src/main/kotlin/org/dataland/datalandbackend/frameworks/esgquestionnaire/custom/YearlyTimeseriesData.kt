package org.dataland.datalandbackend.frameworks.esgquestionnaire.custom

/**
 * Used for gdv fields where some content for the current year is stored together with historical data for years before
 * and forecasted data for years after that current year point of time. TODO
 */
data class YearlyTimeseriesData<ContentDataType>(
    val currentYear: Int,
    val yearlyData: Map<Int, ContentDataType>,
)
