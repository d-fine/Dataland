package org.dataland.datalandbackend.model.eutaxonomy.financials

import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.validator.DataPointMaximumValue
import org.dataland.datalandbackend.validator.DataPointMinimumValue
import java.math.BigDecimal

/**
 * --- API model ---
 * KPIs for Insurance / Reinsurance companies for the EuTaxonomyForFinancials framework
 */
data class InsuranceKpis(
    @DataPointMinimumValue(minimumValue = 0)
    @DataPointMaximumValue(maximumValue = 100)
    val taxonomyEligibleNonLifeInsuranceActivitiesInPercent: ExtendedDataPoint<BigDecimal>? = null,
)
