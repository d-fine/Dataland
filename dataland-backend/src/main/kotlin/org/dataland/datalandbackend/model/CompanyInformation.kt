package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.time.LocalDate

/**
 * --- API model ---
 * Class for defining the request body of a post company request
 * @param companyName name of the company
 * @param headquarters city where the headquarters of the company is located
 * @param industrialSector sector in which the company operates
 * @param marketCap For publicly traded companies: The total monetary value of the outstanding shares of the company
 * @param reportingDateOfMarketCap date to which the market cap value refers
 */
data class CompanyInformation(
    @field:JsonProperty(required = true) val companyName: String = "",
    @field:JsonProperty(required = true) val headquarters: String,
    @field:JsonProperty(required = true) val industrialSector: String,
    @field:JsonProperty(required = true) val marketCap: BigDecimal,
    @field:JsonProperty(required = true) val reportingDateOfMarketCap: LocalDate,
)
