// <--WARNING--> THIS FILE IS AUTO-GENERATED BY THE FRAMEWORK-TOOLBOX AND WILL BE OVERWRITTEN
package org.dataland.datalandbackend.frameworks.eutaxonomyfinancials.model.insuranceReinsurance.averageValueOfTaxonomyAlignedUndertakings

import jakarta.validation.Valid
import org.dataland.datalandbackend.model.datapoints.CurrencyDataPoint
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.validator.MinimumValue
import java.math.BigDecimal

/**
 * The data-model for the AverageValueOfTaxonomyAlignedUndertakings section
 */
@Suppress("MaxLineLength")
data class EutaxonomyFinancialsInsurancereinsuranceAverageValueOfTaxonomyAlignedUndertakings(
    @field:Valid()
    val weightedAverageValueOfAllInvestmentsTurnoverBasedInPercent: ExtendedDataPoint<BigDecimal?>? = null,
    @field:Valid()
    val weightedAverageValueOfAllInvestmentsCapexBasedInPercent: ExtendedDataPoint<BigDecimal?>? = null,
    @field:MinimumValue(minimumValue = 0)
    @field:Valid()
    val weightedAverageValueOfAllInvestmentsTurnoverBasedMonetaryAmount: CurrencyDataPoint? = null,
    @field:MinimumValue(minimumValue = 0)
    @field:Valid()
    val weightedAverageValueOfAllInvestmentsCapexBasedMonetaryAmount: CurrencyDataPoint? = null,
)
