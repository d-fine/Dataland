package org.dataland.datalanduserservice.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.UserServiceOpenApiDescriptionsAndExamples
import org.dataland.datalanduserservice.entity.PortfolioEntity
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import java.time.Instant
import java.util.UUID

/**
 * --- API model ---
 * API model for the responses of HTTP requests
 */
data class BasePortfolio(
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
    override val portfolioName: String,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = UserServiceOpenApiDescriptionsAndExamples.PORTFOLIO_USER_ID_DESCRIPTION,
        example = UserServiceOpenApiDescriptionsAndExamples.PORTFOLIO_USER_ID_EXAMPLE,
    )
    val userId: String,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = UserServiceOpenApiDescriptionsAndExamples.PORTFOLIO_CREATION_TIMESTAMP_DESCRIPTION,
        example = UserServiceOpenApiDescriptionsAndExamples.PORTFOLIO_CREATION_TIMESTAMP_EXAMPLE,
    )
    val creationTimestamp: Long,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = UserServiceOpenApiDescriptionsAndExamples.PORTFOLIO_LAST_UPDATE_TIMESTAMP_DESCRIPTION,
        example = UserServiceOpenApiDescriptionsAndExamples.PORTFOLIO_LAST_UPDATE_TIMESTAMP_EXAMPLE,
    )
    val lastUpdateTimestamp: Long,
    @field:JsonProperty(required = true)
    @field:ArraySchema(
        arraySchema =
            Schema(
                type = "string",
                description = UserServiceOpenApiDescriptionsAndExamples.PORTFOLIO_COMPANY_IDS_DESCRIPTION,
                example = UserServiceOpenApiDescriptionsAndExamples.PORTFOLIO_COMPANY_IDS_EXAMPLE,
            ),
    )
    override val companyIds: Set<String>,
    @field:JsonProperty(required = false)
    @field:Schema(
        description = UserServiceOpenApiDescriptionsAndExamples.PORTFOLIO_IS_MONITORED_DESCRIPTION,
    )
    override val isMonitored: Boolean,
    @field:JsonProperty(required = false)
    @field:Schema(
        description = UserServiceOpenApiDescriptionsAndExamples.PORTFOLIO_STARTING_MONITORING_PERIOD_DESCRIPTION,
        example = UserServiceOpenApiDescriptionsAndExamples.PORTFOLIO_STARTING_MONITORING_PERIOD_EXAMPLE,
    )
    override val startingMonitoringPeriod: String?,
    @field:JsonProperty(required = false)
    @field:ArraySchema(
        arraySchema =
            Schema(
                type = "string",
                description = UserServiceOpenApiDescriptionsAndExamples.PORTFOLIO_MONITORED_FRAMEWORKS_DESCRIPTION,
                example = UserServiceOpenApiDescriptionsAndExamples.PORTFOLIO_MONITORED_FRAMEWORKS_EXAMPLE,
            ),
    )
    override val monitoredFrameworks: Set<String>,
) : Portfolio,
    PortfolioMonitoring {
    constructor(portfolioUpload: PortfolioUpload) : this(
        portfolioId = UUID.randomUUID().toString(),
        portfolioName = portfolioUpload.portfolioName,
        userId = DatalandAuthentication.fromContext().userId,
        creationTimestamp = Instant.now().toEpochMilli(),
        lastUpdateTimestamp = Instant.now().toEpochMilli(),
        companyIds = portfolioUpload.companyIds,
        isMonitored = portfolioUpload.isMonitored,
        startingMonitoringPeriod = portfolioUpload.startingMonitoringPeriod,
        monitoredFrameworks = portfolioUpload.monitoredFrameworks,
    )

    constructor(portfolioMonitoringPatch: PortfolioMonitoringPatch) : this(
        portfolioId = UUID.randomUUID().toString(),
        portfolioName = "",
        userId = DatalandAuthentication.fromContext().userId,
        creationTimestamp = Instant.now().toEpochMilli(),
        lastUpdateTimestamp = Instant.now().toEpochMilli(),
        companyIds = emptySet(),
        isMonitored = portfolioMonitoringPatch.isMonitored,
        startingMonitoringPeriod = portfolioMonitoringPatch.startingMonitoringPeriod,
        monitoredFrameworks = portfolioMonitoringPatch.monitoredFrameworks,
    )

    /**
     * Creates portfolio entity object from BasePortfolio. If parameters are null, the given values remain.
     */
    fun toPortfolioEntity(
        portfolioId: String? = null,
        creationTimestamp: Long = this.creationTimestamp,
        lastUpdateTimestamp: Long = this.lastUpdateTimestamp,
        isMonitored: Boolean = this.isMonitored,
        startingMonitoringPeriod: String? = null,
        monitoredFrameworks: Set<String> = this.monitoredFrameworks,
    ): PortfolioEntity =
        PortfolioEntity(
            portfolioId = portfolioId?.let { UUID.fromString(it) } ?: UUID.fromString(this.portfolioId),
            portfolioName = this.portfolioName,
            userId = this.userId,
            creationTimestamp = creationTimestamp,
            lastUpdateTimestamp = lastUpdateTimestamp,
            companyIds = this.companyIds.toMutableSet(),
            isMonitored = isMonitored,
            startingMonitoringPeriod = startingMonitoringPeriod ?: this.startingMonitoringPeriod,
            monitoredFrameworks = monitoredFrameworks,
        )
}
