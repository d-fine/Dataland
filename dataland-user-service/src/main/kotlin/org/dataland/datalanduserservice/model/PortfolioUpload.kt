package org.dataland.datalanduserservice.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.UserServiceOpenApiDescriptionsAndExamples
import org.dataland.datalanduserservice.utils.MonitoringIsValid

/**
 * --- API model ---
 * Portfolio API model for GET/POST methods
 */
@MonitoringIsValid
data class PortfolioUpload(
    @field:JsonProperty(required = true)
    @field:Schema(
        description = UserServiceOpenApiDescriptionsAndExamples.PORTFOLIO_NAME_DESCRIPTION,
        example = UserServiceOpenApiDescriptionsAndExamples.PORTFOLIO_NAME_EXAMPLE,
    )
    override val portfolioName: String,
    @field:JsonProperty(required = true)
    @field:NotEmpty(message = "Please provide at least one companyId.")
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
    override val isMonitored: Boolean = false,
    @field:JsonProperty(required = false)
    @field:ArraySchema(
        arraySchema =
            Schema(
                type = "string",
                description = UserServiceOpenApiDescriptionsAndExamples.PORTFOLIO_MONITORED_FRAMEWORKS_DESCRIPTION,
                example = UserServiceOpenApiDescriptionsAndExamples.PORTFOLIO_MONITORED_FRAMEWORKS_EXAMPLE,
            ),
    )
    override val monitoredFrameworks: Set<String> = emptySet(),
) : Portfolio,
    PortfolioMonitoring
