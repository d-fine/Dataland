package org.dataland.datalandbackend.entities

import com.fasterxml.jackson.annotation.JsonValue
import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.StoredCompany
import java.math.BigDecimal
import java.time.LocalDate
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "stored_companies")
data class StoredCompanyEntity(
    @Id
    @Column(name = "company_id")
    val companyId: String,

    @Column(name = "company_name")
    var companyName: String,

    @Column(name = "headquarters")
    var headquarters: String,

    @Column(name = "sector")
    var sector: String,

    @Column(name = "market_capitalisation")
    var marketCap: BigDecimal,

    @Column(name = "reporting_date_of_market_capitalisation")
    var reportingDateOfMarketCap: LocalDate,

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
) {
    @JsonValue
    fun toApiModel(): StoredCompany {
        return StoredCompany(
            companyId = companyId,
            companyInformation = CompanyInformation(
                companyName = companyName,
                headquarters = headquarters,
                sector = sector,
                marketCap = marketCap,
                reportingDateOfMarketCap = reportingDateOfMarketCap,
                indices = indices.map { it.toApiModel() }.toMutableSet(),
                identifiers = identifiers.map { it.toApiModel() }.toMutableList(),
                countryCode = countryCode,
                isTeaserCompany = isTeaserCompany,
            ),
            dataRegisteredByDataland = dataRegisteredByDataland.map { it.toApiModel() }.toMutableList()
        )
    }
}
