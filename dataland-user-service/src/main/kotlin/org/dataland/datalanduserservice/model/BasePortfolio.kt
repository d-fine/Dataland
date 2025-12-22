package org.dataland.datalanduserservice.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.UserServiceOpenApiDescriptionsAndExamples
import org.dataland.datalanduserservice.entity.PortfolioEntity
import org.dataland.datalanduserservice.model.enums.NotificationFrequency
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
    override val identifiers: Set<String>,
    @field:JsonProperty(required = false)
    @field:Schema(
        description = UserServiceOpenApiDescriptionsAndExamples.PORTFOLIO_IS_MONITORED_DESCRIPTION,
    )
    override val isMonitored: Boolean,
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
    @field:JsonProperty(required = true)
    @field:Schema(
        description = UserServiceOpenApiDescriptionsAndExamples.PORTFOLIO_NOTIFICATION_FREQUENCY_DESCRIPTION,
        example = UserServiceOpenApiDescriptionsAndExamples.PORTFOLIO_NOTIFICATION_FREQUENCY_EXAMPLE,
    )
    override val notificationFrequency: NotificationFrequency,
    @field:JsonProperty(required = false)
    @field:Schema(
        description = UserServiceOpenApiDescriptionsAndExamples.PORTFOLIO_TIME_WINDOW_THRESHOLD_DESCRIPTION,
        example = UserServiceOpenApiDescriptionsAndExamples.PORTFOLIO_TIME_WINDOW_THRESHOLD_EXAMPLE,
    )
    override val timeWindowThreshold: TimeWindowThreshold?,
    @field:JsonProperty(required = false)
    @field:ArraySchema(
        arraySchema =
            Schema(
                type = "string",
                description = UserServiceOpenApiDescriptionsAndExamples.PORTFOLIO_SHARED_USER_IDS_DESCRIPTION,
                example = UserServiceOpenApiDescriptionsAndExamples.PORTFOLIO_SHARED_USER_IDS_EXAMPLE,
            ),
    )
    override val sharedUserIds: Set<String>,
) : Portfolio,
    PortfolioMonitoring,
    PortfolioSharing {
    constructor(portfolioUpload: PortfolioUpload) : this(
        portfolioId = UUID.randomUUID().toString(),
        portfolioName = portfolioUpload.portfolioName,
        userId = DatalandAuthentication.fromContext().userId,
        creationTimestamp = Instant.now().toEpochMilli(),
        lastUpdateTimestamp = Instant.now().toEpochMilli(),
        identifiers = portfolioUpload.identifiers,
        isMonitored = portfolioUpload.isMonitored,
        monitoredFrameworks = portfolioUpload.monitoredFrameworks,
        notificationFrequency = portfolioUpload.notificationFrequency,
        timeWindowThreshold = portfolioUpload.timeWindowThreshold,
        sharedUserIds = emptySet(),
    )

    constructor(portfolioMonitoringPatch: PortfolioMonitoringPatch) : this(
        portfolioId = UUID.randomUUID().toString(),
        portfolioName = "",
        userId = DatalandAuthentication.fromContext().userId,
        creationTimestamp = Instant.now().toEpochMilli(),
        lastUpdateTimestamp = Instant.now().toEpochMilli(),
        identifiers = emptySet(),
        isMonitored = portfolioMonitoringPatch.isMonitored,
        monitoredFrameworks = portfolioMonitoringPatch.monitoredFrameworks,
        notificationFrequency = portfolioMonitoringPatch.notificationFrequency,
        timeWindowThreshold = portfolioMonitoringPatch.timeWindowThreshold,
        sharedUserIds = emptySet(),
    )

    constructor(portfolioSharingPatch: PortfolioSharingPatch) : this(
        portfolioId = UUID.randomUUID().toString(),
        portfolioName = "",
        userId = DatalandAuthentication.fromContext().userId,
        creationTimestamp = Instant.now().toEpochMilli(),
        lastUpdateTimestamp = Instant.now().toEpochMilli(),
        identifiers = emptySet(),
        isMonitored = false,
        monitoredFrameworks = emptySet(),
        sharedUserIds = portfolioSharingPatch.sharedUserIds,
    )

    /**
     * Creates portfolio entity object from BasePortfolio. If parameters are null, the given values remain.
     */
    @Suppress("LongParameterList")
    fun toPortfolioEntity(
        portfolioId: String? = null,
        creationTimestamp: Long = this.creationTimestamp,
        lastUpdateTimestamp: Long = this.lastUpdateTimestamp,
        isMonitored: Boolean = this.isMonitored,
        monitoredFrameworks: Set<String> = this.monitoredFrameworks,
        notificationFrequency: NotificationFrequency = this.notificationFrequency,
        timeWindowThreshold: TimeWindowThreshold? = this.timeWindowThreshold,
        sharedUserIds: Set<String> = this.sharedUserIds,
    ): PortfolioEntity =
        PortfolioEntity(
            portfolioId = portfolioId?.let { UUID.fromString(it) } ?: UUID.fromString(this.portfolioId),
            portfolioName = this.portfolioName,
            userId = this.userId,
            creationTimestamp = creationTimestamp,
            lastUpdateTimestamp = lastUpdateTimestamp,
            companyIds = this.identifiers.toMutableSet(),
            isMonitored = isMonitored,
            monitoredFrameworks = monitoredFrameworks,
            notificationFrequency = notificationFrequency,
            timeWindowThreshold = timeWindowThreshold,
            sharedUserIds = sharedUserIds,
        )
}
