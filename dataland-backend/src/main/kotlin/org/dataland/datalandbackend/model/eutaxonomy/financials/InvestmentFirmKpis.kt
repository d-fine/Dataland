package org.dataland.datalandbackend.model.eutaxonomy.financials

import jakarta.validation.Valid
import java.math.BigDecimal
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint

/** --- API model --- KPIs for Investment Firms for the EuTaxonomyForFinancials framework */
data class InvestmentFirmKpis(
  @field:Valid() val greenAssetRatioInPercent: ExtendedDataPoint<BigDecimal>? = null
)
