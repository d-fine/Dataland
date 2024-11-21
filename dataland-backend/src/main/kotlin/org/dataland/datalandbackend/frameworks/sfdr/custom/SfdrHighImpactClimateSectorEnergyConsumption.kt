package org.dataland.datalandbackend.frameworks.sfdr.custom

import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import java.math.BigDecimal

/**
 * --- API model ---
 * Custom type for the applicable high-impact climate sectors in the sfdr framework.
 */
data class SfdrHighImpactClimateSectorEnergyConsumption(
    val highImpactClimateSectorEnergyConsumptionInGWh: ExtendedDataPoint<BigDecimal>? = null,
    val highImpactClimateSectorEnergyConsumptionInGWhPerMillionEURRevenue: ExtendedDataPoint<BigDecimal>? = null,
)
