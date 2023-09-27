package org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories

import org.dataland.datalandbackend.model.ExtendedDataPoint
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Energy performance" belonging to the category "Environmental" of the sfdr framework.
 */
data class SfdrEnvironmentalEnergyPerformance(
    val renewableEnergyProductionInGWh: ExtendedDataPoint<BigDecimal>? = null,

    val renewableEnergyConsumptionInGWh: ExtendedDataPoint<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionInGWh: ExtendedDataPoint<BigDecimal>? = null,

    val nonRenewableEnergyProductionInGWh: ExtendedDataPoint<BigDecimal>? = null,

    val highImpactClimateSectorEnergyConsumptionNaceAInGWh: ExtendedDataPoint<BigDecimal>? = null,

    val highImpactClimateSectorEnergyConsumptionNaceBInGWh: ExtendedDataPoint<BigDecimal>? = null,

    val highImpactClimateSectorEnergyConsumptionNaceCInGWh: ExtendedDataPoint<BigDecimal>? = null,

    val highImpactClimateSectorEnergyConsumptionNaceDInGWh: ExtendedDataPoint<BigDecimal>? = null,

    val highImpactClimateSectorEnergyConsumptionNaceEInGWh: ExtendedDataPoint<BigDecimal>? = null,

    val highImpactClimateSectorEnergyConsumptionNaceFInGWh: ExtendedDataPoint<BigDecimal>? = null,

    val highImpactClimateSectorEnergyConsumptionNaceGInGWh: ExtendedDataPoint<BigDecimal>? = null,

    val highImpactClimateSectorEnergyConsumptionNaceHInGWh: ExtendedDataPoint<BigDecimal>? = null,

    val highImpactClimateSectorEnergyConsumptionNaceLInGWh: ExtendedDataPoint<BigDecimal>? = null,

    val totalHighImpactClimateSectorEnergyConsumptionInGWh: ExtendedDataPoint<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionFossilFuelsInGWh: ExtendedDataPoint<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionCrudeOilInGWh: ExtendedDataPoint<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionNaturalGasInGWh: ExtendedDataPoint<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionLigniteInGWh: ExtendedDataPoint<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionCoalInGWh: ExtendedDataPoint<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionNuclearEnergyInGWh: ExtendedDataPoint<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionOtherInGWh: ExtendedDataPoint<BigDecimal>? = null,
)
