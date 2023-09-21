package org.dataland.datalandbackend.model.eutaxonomy.financials

import org.dataland.datalandbackend.model.ExtendedDataPoint
import java.math.BigDecimal

/**
 * --- API model ---
 * KPIs for all companies for the EuTaxonomyForFinancials framework
 */
data class EligibilityKpis(
    val taxonomyEligibleActivity: ExtendedDataPoint<BigDecimal>? = null,

    val taxonomyNonEligibleActivity: ExtendedDataPoint<BigDecimal>? = null,

    val derivatives: ExtendedDataPoint<BigDecimal>? = null,

    val banksAndIssuers: ExtendedDataPoint<BigDecimal>? = null,

    val investmentNonNfrd: ExtendedDataPoint<BigDecimal>? = null,
)
