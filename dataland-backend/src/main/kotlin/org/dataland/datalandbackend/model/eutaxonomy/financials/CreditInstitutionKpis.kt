package org.dataland.datalandbackend.model.eutaxonomy.financials

import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.validator.DataPointMaximumValue
import org.dataland.datalandbackend.validator.DataPointMinimumValue
import java.math.BigDecimal

/**
 * --- API model ---
 * KPIs for credit institutions for the EuTaxonomyForFinancials framework
 */
data class CreditInstitutionKpis(
    @DataPointMinimumValue(minimumValue = 0)
    @DataPointMaximumValue(maximumValue = 100)
    val tradingPortfolioInPercent: ExtendedDataPoint<BigDecimal>? = null,

    @DataPointMinimumValue(minimumValue = 0)
    @DataPointMaximumValue(maximumValue = 100)
    val interbankLoansInPercent: ExtendedDataPoint<BigDecimal>? = null,

    @DataPointMinimumValue(minimumValue = 0)
    @DataPointMaximumValue(maximumValue = 100)
    val tradingPortfolioAndInterbankLoansInPercent: ExtendedDataPoint<BigDecimal>? = null,

    @DataPointMinimumValue(minimumValue = 0)
    @DataPointMaximumValue(maximumValue = 100)
    val greenAssetRatioInPercent: ExtendedDataPoint<BigDecimal>? = null,
)
