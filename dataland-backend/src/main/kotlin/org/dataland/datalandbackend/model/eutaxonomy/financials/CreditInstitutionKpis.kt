package org.dataland.datalandbackend.model.eutaxonomy.financials

import org.dataland.datalandbackend.model.DataPointOneValue
import java.math.BigDecimal

/**
 * --- API model ---
 * KPIs for credit institutions for the EuTaxonomyForFinancials framework
 */
data class CreditInstitutionKpis(
    val tradingPortfolio: DataPointOneValue<BigDecimal>? = null,

    val interbankLoans: DataPointOneValue<BigDecimal>? = null,

    val tradingPortfolioAndInterbankLoans: DataPointOneValue<BigDecimal>? = null,

    val greenAssetRatio: DataPointOneValue<BigDecimal>? = null,
)
