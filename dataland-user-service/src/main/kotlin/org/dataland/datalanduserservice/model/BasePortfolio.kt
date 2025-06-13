package org.dataland.datalanduserservice.model

import com.fasterxml.jackson.annotation.JsonProperty
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
    val portfolioId: String,
    @field:JsonProperty(required = true)
    override val portfolioName: String,
    @field:JsonProperty(required = true)
    val userId: String,
    @field:JsonProperty(required = true)
    val creationTimestamp: Long,
    @field:JsonProperty(required = true)
    val lastUpdateTimestamp: Long,
    @field:JsonProperty(required = true)
    override val companyIds: Set<String>,
    @field:JsonProperty(required = false)
    override val isMonitored: Boolean,
    @field:JsonProperty(required = false)
    override val startingMonitoringPeriod: String?,
    @field:JsonProperty(required = false)
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
        creationTimestamp: Long? = null,
        lastUpdateTimestamp: Long? = null,
        isMonitored: Boolean? = null,
        startingMonitoringPeriod: String? = null,
        monitoredFrameworks: Set<String>? = null,
    ): PortfolioEntity =
        PortfolioEntity(
            portfolioId = portfolioId?.let { UUID.fromString(it) } ?: UUID.fromString(this.portfolioId),
            portfolioName = this.portfolioName,
            userId = this.userId,
            creationTimestamp = creationTimestamp ?: this.creationTimestamp,
            lastUpdateTimestamp = lastUpdateTimestamp ?: this.lastUpdateTimestamp,
            companyIds = this.companyIds.toMutableSet(),
            isMonitored = isMonitored ?: this.isMonitored,
            startingMonitoringPeriod = startingMonitoringPeriod ?: this.startingMonitoringPeriod,
            monitoredFrameworks = monitoredFrameworks ?: this.monitoredFrameworks,
        )
}
