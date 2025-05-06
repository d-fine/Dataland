package org.dataland.datalanduserservice.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
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
    @field:JsonProperty(required = true)
    override val frameworks: Set<DataTypeEnum>,
) : Portfolio {
    constructor(portfolioUpload: PortfolioUpload) : this(
        portfolioId = UUID.randomUUID().toString(),
        portfolioName = portfolioUpload.portfolioName,
        userId = DatalandAuthentication.fromContext().userId,
        creationTimestamp = Instant.now().toEpochMilli(),
        lastUpdateTimestamp = Instant.now().toEpochMilli(),
        companyIds = portfolioUpload.companyIds,
        frameworks = portfolioUpload.frameworks,
    )

    /**
     * Creates portfolio entity object from BasePortfolio.
     * In case of replacing an existing portfolio (PUT), provide portfolioId and creationTimestamp of portfolio to be
     * replaced in order to keep them (and not replace them).
     */
    fun toPortfolioEntity(
        portfolioId: String? = null,
        creationTimestamp: Long? = null,
    ): PortfolioEntity =
        PortfolioEntity(
            portfolioId = portfolioId?.let { UUID.fromString(it) } ?: UUID.fromString(this.portfolioId),
            portfolioName = this.portfolioName,
            userId = this.userId,
            creationTimestamp = creationTimestamp ?: this.creationTimestamp,
            lastUpdateTimestamp = this.lastUpdateTimestamp,
            companyIds = this.companyIds.toMutableSet(),
            frameworks = this.frameworks.toMutableSet(),
        )
}
