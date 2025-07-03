package org.dataland.datalanduserservice.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.UsersOpenApiDescriptionsAndExamples

/**
 * --- API model ---
 * API model for the responses of HTTP requests
 */
data class EnrichedPortfolio(
    @field:JsonProperty(required = true)
    @field:Schema(
        description = UsersOpenApiDescriptionsAndExamples.PORTFOLIO_ID_DESCRIPTION,
        example = UsersOpenApiDescriptionsAndExamples.PORTFOLIO_ID_EXAMPLE,
    )
    val portfolioId: String,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = UsersOpenApiDescriptionsAndExamples.PORTFOLIO_NAME_DESCRIPTION,
        example = UsersOpenApiDescriptionsAndExamples.PORTFOLIO_NAME_EXAMPLE,
    )
    val portfolioName: String,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = UsersOpenApiDescriptionsAndExamples.PORTFOLIO_USER_ID_DESCRIPTION,
        example = UsersOpenApiDescriptionsAndExamples.PORTFOLIO_USER_ID_EXAMPLE,
    )
    val userId: String,
    @field:JsonProperty(required = true)
    val entries: List<EnrichedPortfolioEntry>,
    @field:JsonProperty(required = false)
    @field:Schema(
        description = UsersOpenApiDescriptionsAndExamples.PORTFOLIO_IS_MONITORED_DESCRIPTION,
        example = UsersOpenApiDescriptionsAndExamples.PORTFOLIO_IS_MONITORED_EXAMPLE,
    )
    val isMonitored: Boolean?,
    @field:JsonProperty(required = false)
    @field:Schema(
        description = UsersOpenApiDescriptionsAndExamples.PORTFOLIO_STARTING_MONITORING_PERIOD_DESCRIPTION,
        example = UsersOpenApiDescriptionsAndExamples.PORTFOLIO_STARTING_MONITORING_PERIOD_EXAMPLE,
    )
    val startingMonitoringPeriod: String?,
    @field:JsonProperty(required = false)
    @field:Schema(
        description = UsersOpenApiDescriptionsAndExamples.PORTFOLIO_MONITORED_FRAMEWORKS_DESCRIPTION,
        example = UsersOpenApiDescriptionsAndExamples.PORTFOLIO_MONITORED_FRAMEWORKS_EXAMPLE,
    )
    val monitoredFrameworks: Set<String>?,
)
