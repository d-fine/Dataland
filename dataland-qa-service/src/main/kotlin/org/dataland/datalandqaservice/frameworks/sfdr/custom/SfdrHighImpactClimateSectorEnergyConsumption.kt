package org.dataland.datalandqaservice.frameworks.sfdr.custom

import org.dataland.datalandbackend.openApiClient.model.ExtendedDataPointBigDecimal
import org.dataland.datalandqaservice.model.reports.QaReportDataPoint

/**
 * --- API model ---
 * Custom type for the applicable high-impact climate sectors in the sfdr framework.
 */
data class SfdrHighImpactClimateSectorEnergyConsumption(
    val highImpactClimateSectorEnergyConsumptionInGWh: QaReportDataPoint<ExtendedDataPointBigDecimal>? = null,
    val highImpactClimateSectorEnergyConsumptionInGWhPerMillionEURRevenue: QaReportDataPoint<ExtendedDataPointBigDecimal>? = null,
)
