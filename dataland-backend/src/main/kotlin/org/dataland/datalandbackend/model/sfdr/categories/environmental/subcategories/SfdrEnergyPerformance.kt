package org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories

import org.dataland.datalandbackend.model.DataPointWithUnit
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Energy performance" belonging to the category "Environmental" of the sfdr framework.
 */
data class SfdrEnvironmentalEnergyPerformance(
    val renewableEnergyProduction: DataPointWithUnit<BigDecimal>? = null,

    val renewableEnergyConsumption: DataPointWithUnit<BigDecimal>? = null,

    val nonRenewableEnergyConsumption: DataPointWithUnit<BigDecimal>? = null,

    val nonRenewableEnergyProduction: DataPointWithUnit<BigDecimal>? = null,

    val highImpactClimateSectorEnergyConsumptionNaceA: DataPointWithUnit<BigDecimal>? = null,

    val highImpactClimateSectorEnergyConsumptionNaceB: DataPointWithUnit<BigDecimal>? = null,

    val highImpactClimateSectorEnergyConsumptionNaceC: DataPointWithUnit<BigDecimal>? = null,

    val highImpactClimateSectorEnergyConsumptionNaceD: DataPointWithUnit<BigDecimal>? = null,

    val highImpactClimateSectorEnergyConsumptionNaceE: DataPointWithUnit<BigDecimal>? = null,

    val highImpactClimateSectorEnergyConsumptionNaceF: DataPointWithUnit<BigDecimal>? = null,

    val highImpactClimateSectorEnergyConsumptionNaceG: DataPointWithUnit<BigDecimal>? = null,

    val highImpactClimateSectorEnergyConsumptionNaceH: DataPointWithUnit<BigDecimal>? = null,

    val highImpactClimateSectorEnergyConsumptionNaceL: DataPointWithUnit<BigDecimal>? = null,

    val totalHighImpactClimateSectorEnergyConsumption: DataPointWithUnit<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionFossilFuels: DataPointWithUnit<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionCrudeOil: DataPointWithUnit<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionNaturalGas: DataPointWithUnit<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionLignite: DataPointWithUnit<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionCoal: DataPointWithUnit<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionNuclearEnergy: DataPointWithUnit<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionOther: DataPointWithUnit<BigDecimal>? = null,
)
