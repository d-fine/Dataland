package org.dataland.datalanduserservice.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.UsersOpenApiDescriptionsAndExamples
import org.dataland.datalanduserservice.utils.MonitoringIsValid

/**
 * --- API model ---
 * Portfolio Monitoring API model for PATCH method
 */
@MonitoringIsValid
data class PortfolioMonitoringPatch(
    @field:JsonProperty(required = false)
    @field:Schema(
        description = UsersOpenApiDescriptionsAndExamples.PORTFOLIO_IS_MONITORED_DESCRIPTION,
        example = UsersOpenApiDescriptionsAndExamples.PORTFOLIO_IS_MONITORED_EXAMPLE,
    )
    override val isMonitored: Boolean,
    @field:JsonProperty(required = false)
    @field:Schema(
        description = UsersOpenApiDescriptionsAndExamples.PORTFOLIO_STARTING_MONITORING_PERIOD_DESCRIPTION,
        example = UsersOpenApiDescriptionsAndExamples.PORTFOLIO_STARTING_MONITORING_PERIOD_EXAMPLE,
    )
    override val startingMonitoringPeriod: String?,
    @field:JsonProperty(required = false)
    @field:Schema(
        description = UsersOpenApiDescriptionsAndExamples.PORTFOLIO_MONITORED_FRAMEWORKS_DESCRIPTION,
        example = UsersOpenApiDescriptionsAndExamples.PORTFOLIO_MONITORED_FRAMEWORKS_EXAMPLE,
    )
    override val monitoredFrameworks: Set<String>,
) : PortfolioMonitoring
