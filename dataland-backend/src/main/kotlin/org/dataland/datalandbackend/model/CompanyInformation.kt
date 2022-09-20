package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.enums.company.StockIndex
import java.math.BigDecimal
import java.math.BigInteger
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
    val companyName: String,

    @field:JsonProperty(required = false)
    val headquarters: String? = null,

    @field:JsonProperty(required = false)
    val sector: String? = null,

    @field:JsonProperty(required = false)
    val industry: String? = null,

    @field:JsonProperty(required = false)
    val currency: String?="EUR",

    @field:JsonProperty(required = false)
    val marketCap: BigDecimal?=null,

    @field:JsonProperty(required = false)
    val reportingDateOfMarketCap: LocalDate?=null,

    @field:JsonProperty(required = false)
    val numberOfShares: BigDecimal?=null,

    @field:JsonProperty(required = false)
    val sharePrice: BigDecimal?=null,

    @field:JsonProperty(required = false)
    val numberOfEmployees: BigDecimal?=null,

    @field:JsonProperty(required = false)
    val indices: Set<StockIndex>,

    @field:JsonProperty(required = true)
    val identifiers: List<CompanyIdentifier>,

    @field:JsonProperty(required = true)
    val countryCode: String,

    @get:JsonProperty(value = "isTeaserCompany")
    val isTeaserCompany: Boolean = false,
)
