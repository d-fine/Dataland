package org.dataland.datalandbackend.model.eutaxonomy.financials

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.DataPoint
import java.math.BigDecimal

/**
 * --- API model ---
 * KPIs for all companies for the EuTaxonomyForFinancials framework
 */
data class EligibilityKpis(
    val taxonomyEligibleActivity: DataPoint<BigDecimal>? = null,

    val taxonomyNonEligibleActivity: DataPoint<BigDecimal>? = null,

    val derivatives: DataPoint<BigDecimal>? = null,

    val banksAndIssuers: DataPoint<BigDecimal>? = null,

    val investmentNonNfrd: DataPoint<BigDecimal>? = null,
)
