package org.dataland.datalanduserservice.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * Portfolio API model for GET/POST methods
 */
data class PortfolioPayload(
    @field:JsonProperty(required = true)
    override val portfolioName: String,
    @field:JsonProperty(required = true)
    override val companyIds: Set<String> = emptySet(),
    @field:JsonProperty(required = true)
    override val dataTypes: Set<String> = emptySet(),
) : Portfolio
