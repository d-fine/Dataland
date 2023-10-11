package org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories

import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.enums.sfdr.HighImpactClimateSector
import org.dataland.datalandbackend.utils.JsonExampleFormattingConstants
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Energy performance" belonging to the category "Environmental" of the sfdr framework.
 */
data class SfdrEnvironmentalEnergyPerformance(
    val renewableEnergyProductionInGWh: ExtendedDataPoint<BigDecimal>? = null,

    val renewableEnergyConsumptionInGWh: ExtendedDataPoint<BigDecimal>? = null,

    val nonRenewableEnergyProductionInGWh: ExtendedDataPoint<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionInGWh: ExtendedDataPoint<BigDecimal>? = null,

    @field:Schema(
        implementation = Map::class,
        example = JsonExampleFormattingConstants.HIGH_IMPACT_CLIMATE_SECTORS_DEFAULT_VALUE,
    )
    val applicableHighImpactClimateSector: Map<HighImpactClimateSector, ExtendedDataPoint<BigDecimal>>? = null,

    val nonRenewableEnergyConsumptionFossilFuelsInGWh: ExtendedDataPoint<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionCrudeOilInGWh: ExtendedDataPoint<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionNaturalGasInGWh: ExtendedDataPoint<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionLigniteInGWh: ExtendedDataPoint<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionCoalInGWh: ExtendedDataPoint<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionNuclearEnergyInGWh: ExtendedDataPoint<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionOtherInGWh: ExtendedDataPoint<BigDecimal>? = null,
)
