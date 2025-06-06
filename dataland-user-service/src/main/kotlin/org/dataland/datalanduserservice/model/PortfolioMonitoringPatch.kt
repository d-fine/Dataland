package org.dataland.datalanduserservice.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * Portfolio API model for GET/POST methods
 */
data class PortfolioMonitoringPatch(
    @field:JsonProperty(required = false)
    override val isMonitored: Boolean?,
    @field:JsonProperty(required = false)
    override val startingMonitoringPeriod: String?,
    @field:JsonProperty(required = false)
    // @field:NotEmpty(message = "Please provide at least one framework.")
    override val monitoredFrameworks: MutableSet<String>?,
) : PortfolioMonitoring
