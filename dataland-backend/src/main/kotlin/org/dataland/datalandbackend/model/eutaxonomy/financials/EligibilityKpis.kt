package org.dataland.datalandbackend.model.eutaxonomy.financials

import jakarta.validation.Valid
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import java.math.BigDecimal

/**
 * --- API model ---
 * KPIs for all companies for the EuTaxonomyForFinancials framework
 */
data class EligibilityKpis(
    @field:Valid()
    val taxonomyEligibleActivityInPercent: ExtendedDataPoint<BigDecimal>? = null,
    @field:Valid()
    val taxonomyNonEligibleActivityInPercent: ExtendedDataPoint<BigDecimal>? = null,
    @field:Valid()
    val derivativesInPercent: ExtendedDataPoint<BigDecimal>? = null,
    @field:Valid()
    val banksAndIssuersInPercent: ExtendedDataPoint<BigDecimal>? = null,
    @field:Valid()
    val investmentNonNfrdInPercent: ExtendedDataPoint<BigDecimal>? = null,
)
