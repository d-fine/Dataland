package org.dataland.datalandbackend.model.eutaxonomy.financials

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.DataPoint

/**
 * --- API model ---
 * KPIs for all companies for the EuTaxonomyForFinancials framework
 */
data class EligibilityKpis(
    @field:JsonProperty("taxonomyEligibleActivity")
    val taxonomyEligibleActivity: DataPoint? = null,

    @field:JsonProperty("derivatives")
    val derivatives: DataPoint? = null,

    @field:JsonProperty("banksAndIssuers")
    val banksAndIssuers: DataPoint? = null,

    @field:JsonProperty("investmentNonNfrd")
    val investmentNonNfrd: DataPoint? = null,
)
