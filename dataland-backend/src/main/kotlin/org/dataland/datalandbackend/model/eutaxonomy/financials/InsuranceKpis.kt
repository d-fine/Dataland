package org.dataland.datalandbackend.model.eutaxonomy.financials

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

/**
 * --- API model ---
 * KPIs for Insurance / Reinsurance companies for the EuTaxonomyForFinancials Framework
 */
data class InsuranceKpis(
    @field:JsonProperty("taxonomyEligibleNonLifeInsuranceActivities")
    val taxonomyEligibleNonLifeInsuranceActivities: BigDecimal? = null,
)
