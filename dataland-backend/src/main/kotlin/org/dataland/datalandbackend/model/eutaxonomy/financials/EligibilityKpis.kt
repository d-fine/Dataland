package org.dataland.datalandbackend.model.eutaxonomy.financials

import org.dataland.datalandbackend.model.DataPointOneValue
import java.math.BigDecimal

/**
 * --- API model ---
 * KPIs for all companies for the EuTaxonomyForFinancials framework
 */
data class EligibilityKpis(
    val taxonomyEligibleActivityInPercent: DataPointOneValue<BigDecimal>? = null,

    val taxonomyNonEligibleActivityInPercent: DataPointOneValue<BigDecimal>? = null,

    val derivativesInPercent: DataPointOneValue<BigDecimal>? = null,

    val banksAndIssuersInPercent: DataPointOneValue<BigDecimal>? = null,

    val investmentNonNfrdInPercent: DataPointOneValue<BigDecimal>? = null,
)
