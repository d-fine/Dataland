package org.dataland.datalandbackend.model.eutaxonomy.financials

import jakarta.validation.Valid
import java.math.BigDecimal
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint

/**
 * --- API model --- KPIs for Insurance / Reinsurance companies for the EuTaxonomyForFinancials
 * framework
 */
data class InsuranceKpis(
  @field:Valid()
  val taxonomyEligibleNonLifeInsuranceActivitiesInPercent: ExtendedDataPoint<BigDecimal>? = null
)
