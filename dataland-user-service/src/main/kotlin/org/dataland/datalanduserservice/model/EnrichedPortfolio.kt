package org.dataland.datalanduserservice.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.UserServiceOpenApiDescriptionsAndExamples

/**
 * --- API model ---
 * API model for the responses of HTTP requests
 */
data class EnrichedPortfolio(
    @field:JsonProperty(required = true)
    @field:Schema(
        description = UserServiceOpenApiDescriptionsAndExamples.PORTFOLIO_ID_DESCRIPTION,
        example = UserServiceOpenApiDescriptionsAndExamples.PORTFOLIO_ID_EXAMPLE,
    )
    val portfolioId: String,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = UserServiceOpenApiDescriptionsAndExamples.PORTFOLIO_NAME_DESCRIPTION,
        example = UserServiceOpenApiDescriptionsAndExamples.PORTFOLIO_NAME_EXAMPLE,
    )
    val portfolioName: String,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = UserServiceOpenApiDescriptionsAndExamples.PORTFOLIO_USER_ID_DESCRIPTION,
        example = UserServiceOpenApiDescriptionsAndExamples.PORTFOLIO_USER_ID_EXAMPLE,
    )
    val userId: String,
    @field:JsonProperty(required = true)
    val entries: List<EnrichedPortfolioEntry>,
    @field:JsonProperty(required = false)
    @field:Schema(
        description = UserServiceOpenApiDescriptionsAndExamples.PORTFOLIO_IS_MONITORED_DESCRIPTION,
    )
    val isMonitored: Boolean?,
    @field:JsonProperty(required = false)
    @field:Schema(
        description = UserServiceOpenApiDescriptionsAndExamples.PORTFOLIO_STARTING_MONITORING_PERIOD_DESCRIPTION,
        example = UserServiceOpenApiDescriptionsAndExamples.PORTFOLIO_STARTING_MONITORING_PERIOD_EXAMPLE,
    )
    val startingMonitoringPeriod: String?,
    @field:JsonProperty(required = false)
    @field:ArraySchema(
        arraySchema =
            Schema(
                type = "string",
                description = UserServiceOpenApiDescriptionsAndExamples.PORTFOLIO_MONITORED_FRAMEWORKS_DESCRIPTION,
                example = UserServiceOpenApiDescriptionsAndExamples.PORTFOLIO_MONITORED_FRAMEWORKS_EXAMPLE,
            ),
    )
    val monitoredFrameworks: Set<String>?,
)
