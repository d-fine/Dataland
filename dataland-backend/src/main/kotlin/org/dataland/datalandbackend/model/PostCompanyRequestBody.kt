package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.Date

/**
 * --- API model ---
 * Class for defining the request body of a request containing only a company name
 * @param companyName name of the company
 */
data class PostCompanyRequestBody(
    @field:JsonProperty(required = true) val companyName: String,
    @field:JsonProperty(required = true) val headquarter: String,
    @field:JsonProperty(required = true) val industrialSector: String,
    @field:JsonProperty(required = true) val marketCap: java.math.BigDecimal,
    @field:JsonProperty(required = true) val reportingDateOfMarketCap: Date,
)

