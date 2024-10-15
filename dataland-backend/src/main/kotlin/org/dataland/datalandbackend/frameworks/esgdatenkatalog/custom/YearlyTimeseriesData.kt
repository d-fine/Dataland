package org.dataland.datalandbackend.frameworks.esgdatenkatalog.custom

/**
 * Used for Esg Datenkatalog fields where some content for the current year is stored together with historical data
 * for years before and forecasted data for years after that current year point of time.
 */
data class YearlyTimeseriesData<ContentDataType>(
    val currentYear: Int,
    val yearlyData: Map<Int, ContentDataType>,
)
