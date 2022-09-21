package org.dataland.datalandbackend.model.eutaxonomy.financials

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.DataPoint
import java.math.BigDecimal


/**
 * --- API model ---
 * KPIs for all companies for the EuTaxonomyForFinancials framework
 */
data class EligibilityKpis(
    @field:JsonProperty("taxonomyEligibleActivity")
    val taxonomyEligibleActivity: DataPoint<BigDecimal>? = null,

    @field:JsonProperty("taxonomyNonEligibleActivity")
    val taxonomyNonEligibleActivity: DataPoint<BigDecimal>? = null,

    @field:JsonProperty("derivatives")
    val derivatives: DataPoint<BigDecimal>? = null,

    @field:JsonProperty("banksAndIssuers")
    val banksAndIssuers: DataPoint<BigDecimal>? = null,

    @field:JsonProperty("investmentNonNfrd")
    val investmentNonNfrd: DataPoint<BigDecimal>? = null,
)
