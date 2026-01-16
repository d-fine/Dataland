package org.dataland.datalanduserservice.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.UserServiceOpenApiDescriptionsAndExamples

/**
 * --- API model ---
 * Portfolio Sharing API model for PATCH method
 */
data class PortfolioSharingPatch(
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
) : PortfolioSharing
