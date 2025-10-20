package org.dataland.datalanduserservice.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.UserServiceOpenApiDescriptionsAndExamples

/**
 * --- API model ---
 * API model for the responses of HTTP requests
 */

data class BasePortfolioName(
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
) : PortfolioName {
    constructor(portfolio: BasePortfolio) : this(
        portfolioId = portfolio.portfolioId,
        portfolioName = portfolio.portfolioName,
    )
}
