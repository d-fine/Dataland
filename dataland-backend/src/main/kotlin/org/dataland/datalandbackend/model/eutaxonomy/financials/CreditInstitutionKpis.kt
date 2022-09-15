package org.dataland.datalandbackend.model.eutaxonomy.financials

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.DataPoint

/**
 * --- API model ---
 * KPIs for credit institutions for the EuTaxonomyForFinancials framework
 */
data class CreditInstitutionKpis(
    @field:JsonProperty("tradingPortfolio")
    val tradingPortfolio: DataPoint? = null,

    @field:JsonProperty("interbankLoans")
    val interbankLoans: DataPoint? = null,

    @field:JsonProperty("tradingPortfolioAndInterbankLoans")
    val tradingPortfolioAndInterbankLoans: DataPoint? = null,

    @field:JsonProperty("greenAssetRatio")
    val greenAssetRatio: DataPoint? = null,
)
