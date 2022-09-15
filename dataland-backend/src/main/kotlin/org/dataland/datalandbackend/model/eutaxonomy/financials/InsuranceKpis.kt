package org.dataland.datalandbackend.model.eutaxonomy.financials

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.DataPoint

/**
 * --- API model ---
 * KPIs for Insurance / Reinsurance companies for the EuTaxonomyForFinancials framework
 */
data class InsuranceKpis(
    @field:JsonProperty("taxonomyEligibleNonLifeInsuranceActivities")
    val taxonomyEligibleNonLifeInsuranceActivities: DataPoint? = null,
)
