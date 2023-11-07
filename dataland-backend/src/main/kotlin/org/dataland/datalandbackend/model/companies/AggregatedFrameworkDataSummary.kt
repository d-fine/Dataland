package org.dataland.datalandbackend.model.companies

/**
 * --- API model ---
 * A class that stores aggregated information on the data sets of a company of a specific data type
 */
data class AggregatedFrameworkDataSummary(
    val numberOfProvidedReportingPeriods: Long,
)
