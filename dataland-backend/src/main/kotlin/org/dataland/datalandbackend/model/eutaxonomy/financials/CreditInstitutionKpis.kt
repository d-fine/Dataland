package org.dataland.datalandbackend.model.eutaxonomy.financials

import org.dataland.datalandbackend.model.DataPointOneValue
import java.math.BigDecimal

/**
 * --- API model ---
 * KPIs for credit institutions for the EuTaxonomyForFinancials framework
 */
data class CreditInstitutionKpis(
    val tradingPortfolioInPercent: DataPointOneValue<BigDecimal>? = null,

    val interbankLoansInPercent: DataPointOneValue<BigDecimal>? = null,

    val tradingPortfolioAndInterbankLoansInPercent: DataPointOneValue<BigDecimal>? = null,

    val greenAssetRatioInPercent: DataPointOneValue<BigDecimal>? = null,
)
