package org.dataland.datalandbackend.model.eutaxonomy.financials

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

/**
 * --- API model ---
 * KPIs for credit institutions for the EuTaxonomyForFinancials framework
 */
data class CreditInstitutionKpis(
    @field:JsonProperty("tradingPortfolio")
    val tradingPortfolio: BigDecimal? = null,

    @field:JsonProperty("interbankLoans")
    val interbankLoans: BigDecimal? = null,

    @field:JsonProperty("tradingPortfolioAndInterbankLoans")
    val tradingPortfolioAndInterbankLoans: BigDecimal? = null,
)
