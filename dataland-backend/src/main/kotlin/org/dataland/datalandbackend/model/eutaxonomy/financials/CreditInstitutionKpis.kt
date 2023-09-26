package org.dataland.datalandbackend.model.eutaxonomy.financials

import org.dataland.datalandbackend.model.ExtendedDataPoint
import java.math.BigDecimal

/**
 * --- API model ---
 * KPIs for credit institutions for the EuTaxonomyForFinancials framework
 */
data class CreditInstitutionKpis(
    val tradingPortfolioInPercent: ExtendedDataPoint<BigDecimal>? = null,

    val interbankLoansInPercent: ExtendedDataPoint<BigDecimal>? = null,

    val tradingPortfolioAndInterbankLoansInPercent: ExtendedDataPoint<BigDecimal>? = null,

    val greenAssetRatioInPercent: ExtendedDataPoint<BigDecimal>? = null,
)
