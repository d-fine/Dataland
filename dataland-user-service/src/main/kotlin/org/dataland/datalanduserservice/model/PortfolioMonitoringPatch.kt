package org.dataland.datalanduserservice.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalanduserservice.utils.MonitoringIsValid

/**
 * --- API model ---
 * Portfolio Monitoring API model for PATCH method
 */
@MonitoringIsValid
data class PortfolioMonitoringPatch(
    @field:JsonProperty(required = false)
    override val isMonitored: Boolean,
    @field:JsonProperty(required = false)
    override val startingMonitoringPeriod: String?,
    @field:JsonProperty(required = false)
    override val monitoredFrameworks: Set<String>,
) : PortfolioMonitoring
