package org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories

import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
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

    val applicableHighImpactClimateSector: ExtendedDataPoint<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionFossilFuelsInGWh: ExtendedDataPoint<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionCrudeOilInGWh: ExtendedDataPoint<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionNaturalGasInGWh: ExtendedDataPoint<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionLigniteInGWh: ExtendedDataPoint<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionCoalInGWh: ExtendedDataPoint<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionNuclearEnergyInGWh: ExtendedDataPoint<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionOtherInGWh: ExtendedDataPoint<BigDecimal>? = null,

    val highImpactClimateSectorEnergyConsumptionNaceA: ExtendedDataPoint<BigDecimal>? = null,
    val highImpactClimateSectorEnergyConsumptionNaceB: ExtendedDataPoint<BigDecimal>? = null,
    val highImpactClimateSectorEnergyConsumptionNaceC: ExtendedDataPoint<BigDecimal>? = null,
    val highImpactClimateSectorEnergyConsumptionNaceD: ExtendedDataPoint<BigDecimal>? = null,
    val highImpactClimateSectorEnergyConsumptionNaceE: ExtendedDataPoint<BigDecimal>? = null,
    val highImpactClimateSectorEnergyConsumptionNaceF: ExtendedDataPoint<BigDecimal>? = null,
    val highImpactClimateSectorEnergyConsumptionNaceG: ExtendedDataPoint<BigDecimal>? = null,
    val highImpactClimateSectorEnergyConsumptionNaceH: ExtendedDataPoint<BigDecimal>? = null,
    val highImpactClimateSectorEnergyConsumptionNaceL: ExtendedDataPoint<BigDecimal>? = null,
)
