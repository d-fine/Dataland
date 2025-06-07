package org.dataland.datalanduserservice.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * API model for the responses of HTTP requests
 */
data class EnrichedPortfolio(
    @field:JsonProperty(required = true)
    val portfolioId: String,
    @field:JsonProperty(required = true)
    val portfolioName: String,
    @field:JsonProperty(required = true)
    val userId: String,
    @field:JsonProperty(required = true)
    val entries: List<EnrichedPortfolioEntry>,
    @field:JsonProperty(required = false)
    val isMonitored: Boolean?,
    @field:JsonProperty(required = false)
    val startingMonitoringPeriod: String?,
    @field:JsonProperty(required = false)
    val monitoredFrameworks: Set<String>?,
)
