package org.dataland.datalandbackend.entities

import com.fasterxml.jackson.annotation.JsonValue
import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.StoredCompany
import org.dataland.datalandbackend.model.enums.company.StockIndex
import java.math.BigDecimal
import java.time.LocalDate
import javax.persistence.*

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

    @ElementCollection(targetClass = StockIndex::class)
    @JoinTable(name = "stored_company_stock_indices", joinColumns = [JoinColumn(name = "company_id")])
    @Column(name = "stock_index", nullable = false)
    @Enumerated(EnumType.STRING)
    var indices: MutableSet<StockIndex>,

    @OneToMany(mappedBy = "company")
    var identifiers: MutableList<CompanyIdentifierEntity>,

    @Column(name = "country_code")
    var countryCode: String
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
                indices = indices,
                identifiers = identifiers.map { it.toApiModel() }.toMutableList(),
                countryCode = countryCode,
            ),
            dataRegisteredByDataland = mutableListOf() // TODO: Implement
        )
    }
}