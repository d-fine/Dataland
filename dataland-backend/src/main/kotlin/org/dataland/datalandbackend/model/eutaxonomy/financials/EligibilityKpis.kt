package org.dataland.datalandbackend.model.eutaxonomy.financials

import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import java.math.BigDecimal

/**
 * --- API model ---
 * KPIs for all companies for the EuTaxonomyForFinancials framework
 */
data class EligibilityKpis(
    val taxonomyEligibleActivityInPercent: ExtendedDataPoint<BigDecimal>? = null,

    val taxonomyNonEligibleActivityInPercent: ExtendedDataPoint<BigDecimal>? = null,

    val derivativesInPercent: ExtendedDataPoint<BigDecimal>? = null,

    val banksAndIssuersInPercent: ExtendedDataPoint<BigDecimal>? = null,

    val investmentNonNfrdInPercent: ExtendedDataPoint<BigDecimal>? = null,
)
