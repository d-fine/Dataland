package org.dataland.datalandbackend.model.eutaxonomy.financials

import org.dataland.datalandbackend.model.DataPoint
import java.math.BigDecimal

/**
 * --- API model ---
 * KPIs for Investment Firms for the EuTaxonomyForFinancials framework
 */

data class InvestmentFirmKpis(
    val greenAssetRatioInvestmentFirm: DataPoint<BigDecimal>? = null,
)
