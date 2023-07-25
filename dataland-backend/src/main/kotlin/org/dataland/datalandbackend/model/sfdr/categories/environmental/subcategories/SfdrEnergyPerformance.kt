package org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories

import org.dataland.datalandbackend.model.DataPoint
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Energy performance" belonging to the category "Environmental" of the sfdr framework.
 */
data class SfdrEnvironmentalEnergyPerformance(
    val renewableEnergyProduction: DataPoint<BigDecimal>? = null,

    val renewableEnergyConsumption: DataPoint<BigDecimal>? = null,

    val nonRenewableEnergyConsumption: DataPoint<BigDecimal>? = null,

    val nonRenewableEnergyProduction: DataPoint<BigDecimal>? = null,

    val highImpactClimateSectorEnergyConsumptionNaceA: DataPoint<BigDecimal>? = null,

    val highImpactClimateSectorEnergyConsumptionNaceB: DataPoint<BigDecimal>? = null,

    val highImpactClimateSectorEnergyConsumptionNaceC: DataPoint<BigDecimal>? = null,

    val highImpactClimateSectorEnergyConsumptionNaceD: DataPoint<BigDecimal>? = null,

    val highImpactClimateSectorEnergyConsumptionNaceE: DataPoint<BigDecimal>? = null,

    val highImpactClimateSectorEnergyConsumptionNaceF: DataPoint<BigDecimal>? = null,

    val highImpactClimateSectorEnergyConsumptionNaceG: DataPoint<BigDecimal>? = null,

    val highImpactClimateSectorEnergyConsumptionNaceH: DataPoint<BigDecimal>? = null,

    val highImpactClimateSectorEnergyConsumptionNaceL: DataPoint<BigDecimal>? = null,

    val totalHighImpactClimateSectorEnergyConsumption: DataPoint<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionFossilFuels: DataPoint<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionCrudeOil: DataPoint<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionNaturalGas: DataPoint<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionLignite: DataPoint<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionCoal: DataPoint<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionNuclearEnergy: DataPoint<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionOther: DataPoint<BigDecimal>? = null,
)
