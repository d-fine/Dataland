package org.dataland.datalandbackend.model.eutaxonomy.financials

import org.dataland.datalandbackend.model.ExtendedDataPoint
import java.math.BigDecimal

/**
 * --- API model ---
 * KPIs for Insurance / Reinsurance companies for the EuTaxonomyForFinancials framework
 */
data class InsuranceKpis(
    val taxonomyEligibleNonLifeInsuranceActivitiesInPercent: ExtendedDataPoint<BigDecimal>? = null,
)
