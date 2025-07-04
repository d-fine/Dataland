package org.dataland.datalanduserservice.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotEmpty
import org.dataland.datalanduserservice.utils.MonitoringIsValid

/**
 * --- API model ---
 * Portfolio API model for GET/POST methods
 */
@MonitoringIsValid
data class PortfolioUpload(
    @field:JsonProperty(required = true)
    override val portfolioName: String,
    @field:JsonProperty(required = true)
    @field:NotEmpty(message = "Please provide at least one companyId.")
    override val companyIds: Set<String>,
    @field:JsonProperty(required = false)
    override val isMonitored: Boolean = false,
    @field:JsonProperty(required = false)
    override val startingMonitoringPeriod: String? = null,
    @field:JsonProperty(required = false)
    override val monitoredFrameworks: Set<String> = emptySet(),
) : Portfolio,
    PortfolioMonitoring
