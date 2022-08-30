package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.enums.company.StockIndex
import java.math.BigDecimal
import java.time.LocalDate

/**
 * --- API model ---
 * Class for defining the request body of a post company request
 * @param companyName name of the company
 * @param headquarters city where the headquarters of the company is located
 * @param sector in which the company operates
 * @param marketCap For publicly traded companies: The total monetary value of the outstanding shares of the company
 * @param reportingDateOfMarketCap date to which the market cap value refers
 */
data class CompanyInformation(
    @field:JsonProperty(required = true)
    var companyName: String,

    @field:JsonProperty(required = true)
    var headquarters: String,

    @field:JsonProperty(required = true)
    var sector: String,

    @field:JsonProperty(required = true)
    var marketCap: BigDecimal,

    @field:JsonProperty(required = true)
    var reportingDateOfMarketCap: LocalDate,

    @field:JsonProperty(required = false)
    var indices: MutableSet<StockIndex>,

    @field:JsonProperty(required = true)
    var identifiers: MutableList<CompanyIdentifier>,

    @field:JsonProperty(required = true)
    var countryCode: String,

    @field:JsonProperty(required = true)
    var isTeaserCompany: Boolean
)
