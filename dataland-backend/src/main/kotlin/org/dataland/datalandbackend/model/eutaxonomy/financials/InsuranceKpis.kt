package org.dataland.datalandbackend.model.eutaxonomy.financials

import jakarta.validation.Valid
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import java.math.BigDecimal

/**
 * --- API model ---
 * KPIs for Insurance / Reinsurance companies for the EuTaxonomyForFinancials framework
 */
data class InsuranceKpis(
    @field:Valid()
    val taxonomyEligibleNonLifeInsuranceActivitiesInPercent: ExtendedDataPoint<BigDecimal>? = null,
)
