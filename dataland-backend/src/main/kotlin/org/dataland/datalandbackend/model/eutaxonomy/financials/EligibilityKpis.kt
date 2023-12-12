package org.dataland.datalandbackend.model.eutaxonomy.financials

import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.validator.DataPointMaximumValue
import org.dataland.datalandbackend.validator.DataPointMinimumValue
import java.math.BigDecimal

/**
 * --- API model ---
 * KPIs for all companies for the EuTaxonomyForFinancials framework
 */
data class EligibilityKpis(
    @DataPointMinimumValue(minimumValue = 0)
    @DataPointMaximumValue(maximumValue = 100)
    val taxonomyEligibleActivityInPercent: ExtendedDataPoint<BigDecimal>? = null,

    @DataPointMinimumValue(minimumValue = 0)
    @DataPointMaximumValue(maximumValue = 100)
    val taxonomyNonEligibleActivityInPercent: ExtendedDataPoint<BigDecimal>? = null,

    @DataPointMinimumValue(minimumValue = 0)
    @DataPointMaximumValue(maximumValue = 100)
    val derivativesInPercent: ExtendedDataPoint<BigDecimal>? = null,

    @DataPointMinimumValue(minimumValue = 0)
    @DataPointMaximumValue(maximumValue = 100)
    val banksAndIssuersInPercent: ExtendedDataPoint<BigDecimal>? = null,

    @DataPointMinimumValue(minimumValue = 0)
    @DataPointMaximumValue(maximumValue = 100)
    val investmentNonNfrdInPercent: ExtendedDataPoint<BigDecimal>? = null,
)
