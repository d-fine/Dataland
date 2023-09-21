package org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories

import org.dataland.datalandbackend.model.CurrencyDataPoint
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Energy performance" belonging to the category "Environmental" of the sfdr framework.
 */
data class SfdrEnvironmentalEnergyPerformance(
    val renewableEnergyProduction: CurrencyDataPoint<BigDecimal>? = null,

    val renewableEnergyConsumption: CurrencyDataPoint<BigDecimal>? = null,

    val nonRenewableEnergyConsumption: CurrencyDataPoint<BigDecimal>? = null,

    val nonRenewableEnergyProduction: CurrencyDataPoint<BigDecimal>? = null,

    val highImpactClimateSectorEnergyConsumptionNaceA: CurrencyDataPoint<BigDecimal>? = null,

    val highImpactClimateSectorEnergyConsumptionNaceB: CurrencyDataPoint<BigDecimal>? = null,

    val highImpactClimateSectorEnergyConsumptionNaceC: CurrencyDataPoint<BigDecimal>? = null,

    val highImpactClimateSectorEnergyConsumptionNaceD: CurrencyDataPoint<BigDecimal>? = null,

    val highImpactClimateSectorEnergyConsumptionNaceE: CurrencyDataPoint<BigDecimal>? = null,

    val highImpactClimateSectorEnergyConsumptionNaceF: CurrencyDataPoint<BigDecimal>? = null,

    val highImpactClimateSectorEnergyConsumptionNaceG: CurrencyDataPoint<BigDecimal>? = null,

    val highImpactClimateSectorEnergyConsumptionNaceH: CurrencyDataPoint<BigDecimal>? = null,

    val highImpactClimateSectorEnergyConsumptionNaceL: CurrencyDataPoint<BigDecimal>? = null,

    val totalHighImpactClimateSectorEnergyConsumption: CurrencyDataPoint<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionFossilFuels: CurrencyDataPoint<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionCrudeOil: CurrencyDataPoint<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionNaturalGas: CurrencyDataPoint<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionLignite: CurrencyDataPoint<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionCoal: CurrencyDataPoint<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionNuclearEnergy: CurrencyDataPoint<BigDecimal>? = null,

    val nonRenewableEnergyConsumptionOther: CurrencyDataPoint<BigDecimal>? = null,
)
