package org.dataland.datalandbackend.model.eutaxonomy.financials

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.DataPoint
import java.math.BigDecimal


/**
 * --- API model ---
 * KPIs for credit institutions for the EuTaxonomyForFinancials framework
 */
data class CreditInstitutionKpis(
    @field:JsonProperty("tradingPortfolio")
    val tradingPortfolio: DataPoint<BigDecimal>? = null,

    @field:JsonProperty("interbankLoans")
    val interbankLoans: DataPoint<BigDecimal>? = null,

    @field:JsonProperty("tradingPortfolioAndInterbankLoans")
    val tradingPortfolioAndInterbankLoans: DataPoint<BigDecimal>? = null,

)
