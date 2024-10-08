// <--WARNING--> THIS FILE IS AUTO-GENERATED BY THE FRAMEWORK-TOOLBOX AND WILL BE OVERWRITTEN
package org.dataland.datalandqaservice.frameworks.eutaxonomyfinancials.model.insuranceReinsurance.averageValueOfTaxonomyAlignedUndertakings

import jakarta.validation.Valid
import org.dataland.datalandbackend.openApiClient.model.CurrencyDataPoint
import org.dataland.datalandbackend.openApiClient.model.ExtendedDataPointBigDecimal
import org.dataland.datalandqaservice.model.reports.QaReportDataPoint

/**
 * The QA-model for the AverageValueOfTaxonomyAlignedUndertakings section
 */
@Suppress("MaxLineLength")
data class EutaxonomyFinancialsInsurancereinsuranceAverageValueOfTaxonomyAlignedUndertakings(
    @field:Valid()
    val weightedAverageValueOfAllInvestmentsTurnoverBasedInPercent: QaReportDataPoint<ExtendedDataPointBigDecimal?>? = null,
    @field:Valid()
    val weightedAverageValueOfAllInvestmentsCapexBasedInPercent: QaReportDataPoint<ExtendedDataPointBigDecimal?>? = null,
    val weightedAverageValueOfAllInvestmentsTurnoverBasedMonetaryAmount: QaReportDataPoint<CurrencyDataPoint?>? = null,
    val weightedAverageValueOfAllInvestmentsCapexBasedMonetaryAmount: QaReportDataPoint<CurrencyDataPoint?>? = null,
)
