package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.enums.company.StockIndex
import org.hibernate.annotations.NotFound
import java.math.BigDecimal
import java.time.LocalDate
import java.util.EnumSet
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.OneToMany

/**
 * --- API model ---
 * Class for defining the request body of a post company request
 * @param companyName name of the company
 * @param headquarters city where the headquarters of the company is located
 * @param sector in which the company operates
 * @param marketCap For publicly traded companies: The total monetary value of the outstanding shares of the company
 * @param reportingDateOfMarketCap date to which the market cap value refers
 */
@Embeddable
data class CompanyInformation(
    @Column(name = "company_name")
    @field:JsonProperty(required = true)
    var companyName: String,

    @Column(name = "headquarters")
    @field:JsonProperty(required = true)
    var headquarters: String,

    @Column(name = "sector")
    @field:JsonProperty(required = true)
    var sector: String,

    @Column(name = "market_capitalisation")
    @field:JsonProperty(required = true)
    var marketCap: BigDecimal,

    @Column(name = "reporting_date_of_market_capitalisation")
    @field:JsonProperty(required = true)
    var reportingDateOfMarketCap: LocalDate,

    @Transient
    @field:JsonProperty(required = false)
    var indices: EnumSet<StockIndex>,

    @OneToMany(mappedBy = "company")
    @field:JsonProperty(required = true)
    var identifiers: MutableList<CompanyIdentifier>,

    @Column(name = "country_code")
    @field:JsonProperty(required = true)
    var countryCode: String
)
