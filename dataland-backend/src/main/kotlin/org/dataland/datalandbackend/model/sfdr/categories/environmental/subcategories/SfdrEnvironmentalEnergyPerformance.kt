package org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories

import org.dataland.datalandbackend.model.DataPointWithUnit
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Energy performance" belonging to the category "Environmental" of the sfdr framework.
 */
data class SfdrEnvironmentalEnergyPerformance(
    val renewableEnergyProductionInGWh: DataPointWithUnit<BigDecimal>? = null,

    val renewableEnergyConsumptionInGWh: DataPointWithUnit<BigDecimal>? = null,

    val nonRenewableEnergyProductionInGWh: DataPointWithUnit<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionInGWh: DataPointWithUnit<BigDecimal>? = null,

    val applicableHighImpactClimateSector: DataPointWithUnit<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionFossilFuelsInGWh: DataPointWithUnit<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionCrudeOilInGWh: DataPointWithUnit<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionNaturalGasInGWh: DataPointWithUnit<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionLigniteInGWh: DataPointWithUnit<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionCoalInGWh: DataPointWithUnit<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionNuclearEnergyInGWh: DataPointWithUnit<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionOtherInGWh: DataPointWithUnit<BigDecimal>? = null,
)
