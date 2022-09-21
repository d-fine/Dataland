package org.dataland.datalandbackend.entities

import com.fasterxml.jackson.annotation.JsonValue
import org.dataland.datalandbackend.interfaces.ApiModelConversion
import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.StoredCompany
import java.math.BigDecimal
import java.time.LocalDate
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table

/**
 * The entity storing data regarding a company stored in dataland
 */
@Entity
@Table(name = "stored_companies")
data class StoredCompanyEntity(
    @Id
    @Column(name = "company_id")
    val companyId: String,

    @Column(name = "company_name")
    var companyName: String,

    @Column(name = "headquarters")
    var headquarters: String?,

    @Column(name = "sector")
    var sector: String?,

    @Column(name = "industry")
    var industry: String?,

    @Column(name = "currency")
    var currency: String?,

    @Column(name = "market_capitalisation")
    var marketCap: BigDecimal?,

    @Column(name = "reporting_date_of_market_capitalisation")
    var reportingDateOfMarketCap: LocalDate?,

    @Column(name = "number_of_shares")
    var numberOfShares: BigDecimal?,

    @Column(name = "share_price")
    var sharePrice: BigDecimal?,

    @Column(name = "number_of_employees")
    var numberOfEmployees: BigDecimal?,

    @OneToMany(mappedBy = "company")
    var indices: MutableSet<StoredCompanyStockIndexEntity>,

    @OneToMany(mappedBy = "company")
    var identifiers: MutableList<CompanyIdentifierEntity>,

    @OneToMany(mappedBy = "company")
    val dataRegisteredByDataland: MutableList<DataMetaInformationEntity>,

    @Column(name = "country_code")
    var countryCode: String,

    @Column(name = "is_teaser_company")
    var isTeaserCompany: Boolean
) : ApiModelConversion<StoredCompany> {
    @JsonValue
    override fun toApiModel(): StoredCompany {
        return StoredCompany(
            companyId = companyId,
            companyInformation = CompanyInformation(
                companyName = companyName,
                headquarters = headquarters,
                sector = sector,
                industry = industry,
                currency = currency,
                marketCap = marketCap,
                reportingDateOfMarketCap = reportingDateOfMarketCap,
                numberOfShares = numberOfShares,
                sharePrice = sharePrice,
                numberOfEmployees = numberOfEmployees,
                indices = indices.map { it.toApiModel() }.toSet(),
                identifiers = identifiers.map { it.toApiModel() }.toList(),
                countryCode = countryCode,
                isTeaserCompany = isTeaserCompany,
            ),
            dataRegisteredByDataland = dataRegisteredByDataland.map { it.toApiModel() }.toMutableList()
        )
    }
}
