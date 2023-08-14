package org.dataland.datalandbackend.model.eutaxonomy.financials

import org.dataland.datalandbackend.model.DataPointOneValue
import java.math.BigDecimal

/**
 * --- API model ---
 * KPIs for all companies for the EuTaxonomyForFinancials framework
 */
data class EligibilityKpis(
    val taxonomyEligibleActivity: DataPointOneValue<BigDecimal>? = null,

    val taxonomyNonEligibleActivity: DataPointOneValue<BigDecimal>? = null,

    val derivatives: DataPointOneValue<BigDecimal>? = null,

    val banksAndIssuers: DataPointOneValue<BigDecimal>? = null,

    val investmentNonNfrd: DataPointOneValue<BigDecimal>? = null,
)
