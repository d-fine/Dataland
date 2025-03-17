package org.dataland.datalanduserservice.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotEmpty
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum

/**
 * --- API model ---
 * Portfolio API model for GET/POST methods
 */
data class PortfolioPayload(
    @field:JsonProperty(required = true)
    override val portfolioName: String,
    @field:JsonProperty(required = true)
    @field:NotEmpty(message = "Please provide at least one companyId.")
    override val companyIds: Set<String>,
    @field:JsonProperty(required = true)
    @field:NotEmpty(message = "Please provide at least one dataType.")
    override val dataTypes: Set<DataTypeEnum>,
) : Portfolio
