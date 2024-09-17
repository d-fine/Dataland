package org.dataland.datalandbackend.model.eutaxonomy.financials

import jakarta.validation.Valid
import java.math.BigDecimal
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint

/** --- API model --- KPIs for credit institutions for the EuTaxonomyForFinancials framework */
data class CreditInstitutionKpis(
  @field:Valid() val tradingPortfolioInPercent: ExtendedDataPoint<BigDecimal>? = null,
  @field:Valid() val interbankLoansInPercent: ExtendedDataPoint<BigDecimal>? = null,
  @field:Valid()
  val tradingPortfolioAndInterbankLoansInPercent: ExtendedDataPoint<BigDecimal>? = null,
  @field:Valid() val greenAssetRatioInPercent: ExtendedDataPoint<BigDecimal>? = null,
)
