package org.dataland.datalanduserservice.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalanduserservice.utils.MonitoringIsValid
import org.dataland.datalanduserservice.utils.PortfolioIsMonitored
import org.dataland.datalanduserservice.utils.PortfolioMonitoredFrameworks
import org.dataland.datalanduserservice.utils.PortfolioStartingMonitoringPeriod

/**
 * --- API model ---
 * Portfolio Monitoring API model for PATCH method
 */
@MonitoringIsValid
data class PortfolioMonitoringPatch(
    @field:JsonProperty(required = false)
    @field:PortfolioIsMonitored
    override val isMonitored: Boolean,
    @field:JsonProperty(required = false)
    @field:PortfolioStartingMonitoringPeriod
    override val startingMonitoringPeriod: String?,
    @field:JsonProperty(required = false)
    @field:PortfolioMonitoredFrameworks
    override val monitoredFrameworks: Set<String>,
) : PortfolioMonitoring
