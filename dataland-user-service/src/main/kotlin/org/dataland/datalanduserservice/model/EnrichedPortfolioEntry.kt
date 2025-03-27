package org.dataland.datalanduserservice.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * API model for a single entry in the enriched portfolio
 */
data class EnrichedPortfolioEntry(
    @field:JsonProperty(required = true)
    val companyId: String,
    @field:JsonProperty(required = true)
    val companyName: String,
    @field:JsonProperty(required = true)
    val sector: String,
    @field:JsonProperty(required = true)
    val countryCode: String,
    @field:JsonProperty(required = false)
    val framework: String?,
    @field:JsonProperty(required = true)
    val companyCockpitRef: String,
    @field:JsonProperty(required = false)
    val frameworkDataRef: String?,
    @field:JsonProperty(required = false)
    val latestReportingPeriod: String?,
)
