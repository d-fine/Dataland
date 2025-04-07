package org.dataland.datalanduserservice.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * API model for the responses of HTTP requests
 */


data class BasePortfolioName(
    @field:JsonProperty(required = true)
    val portfolioId: String,
    @field:JsonProperty(required = true)
    override val portfolioName: String,
) : PortfolioName {
    constructor(portfolio: BasePortfolio) : this(
        portfolioId = portfolio.portfolioId,
        portfolioName = portfolio.portfolioName,
    )
}

