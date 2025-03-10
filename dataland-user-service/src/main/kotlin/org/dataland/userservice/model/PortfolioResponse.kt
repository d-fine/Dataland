package org.dataland.userservice.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * API model for the responses of HTTP requests
 */
data class PortfolioResponse(
    @field:JsonProperty(required = true)
    val portfolioId: String,
    @field:JsonProperty(required = true)
    override val portfolioName: String,
    @field:JsonProperty(required = true)
    override val userId: String,
    @field:JsonProperty(required = true)
    val creationTimestamp: Long,
    @field:JsonProperty(required = true)
    val lastUpdateTimestamp: Long,
    @field:JsonProperty(required = true)
    override val companyIds: Set<String>,
    @field:JsonProperty(required = true)
    override val dataTypes: Set<String>,
) : Portfolio
