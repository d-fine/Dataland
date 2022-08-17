package org.dataland.datalandbackend.model.eutaxonomy.financials

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

/**
 * --- API model ---
 * KPIs for all companies for the EuTaxonomyForFinancials Framework
 */
data class EligibilityKpis(
    @field:JsonProperty("taxonomyEligibleActivity")
    val taxonomyEligibleActivity: BigDecimal? = null,

    @field:JsonProperty("derivatives")
    val derivatives: BigDecimal? = null,

    @field:JsonProperty("banksAndIssuers")
    val banksAndIssuers: BigDecimal? = null,

    @field:JsonProperty("investmentNonNfrd")
    val investmentNonNfrd: BigDecimal? = null,
)
