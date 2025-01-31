package org.dataland.datalandbackend.model.companies

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * A class that stores aggregated information on the datasets of a company of a specific data type
 */
data class AggregatedFrameworkDataSummary(
    @field:JsonProperty(required = true)
    val numberOfProvidedReportingPeriods: Long,
)
