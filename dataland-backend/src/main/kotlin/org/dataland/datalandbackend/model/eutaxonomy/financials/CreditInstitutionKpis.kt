package org.dataland.datalandbackend.model.eutaxonomy.financials

import org.dataland.datalandbackend.model.ExtendedDataPoint
import java.math.BigDecimal

/**
 * --- API model ---
 * KPIs for credit institutions for the EuTaxonomyForFinancials framework
 */
data class CreditInstitutionKpis(
    val tradingPortfolio: ExtendedDataPoint<BigDecimal>? = null,

    val interbankLoans: ExtendedDataPoint<BigDecimal>? = null,

    val tradingPortfolioAndInterbankLoans: ExtendedDataPoint<BigDecimal>? = null,

    val greenAssetRatio: ExtendedDataPoint<BigDecimal>? = null,
)
