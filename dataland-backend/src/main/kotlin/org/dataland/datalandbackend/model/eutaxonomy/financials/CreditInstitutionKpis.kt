package org.dataland.datalandbackend.model.eutaxonomy.financials

import org.dataland.datalandbackend.model.DataPoint
import java.math.BigDecimal

/**
 * --- API model ---
 * KPIs for credit institutions for the EuTaxonomyForFinancials framework
 */
data class CreditInstitutionKpis(
    val tradingPortfolio: DataPoint<BigDecimal>? = null,

    val interbankLoans: DataPoint<BigDecimal>? = null,

    val tradingPortfolioAndInterbankLoans: DataPoint<BigDecimal>? = null,

    val greenAssetRatio: DataPoint<BigDecimal>? = null,
)
