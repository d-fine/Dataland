package org.dataland.datalandbackend.model.sfdr.submodels

import org.dataland.datalandbackend.model.DataPoint
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the SFDR questionnaire regarding the impact topic "Energy performance"
 */
data class SfdrEnergyPerformance(
    val renewableEnergyProduction: DataPoint<BigDecimal>?,

    val renewableEnergyConsumption: DataPoint<BigDecimal>?,

    val nonRenewableEnergyConsumption: DataPoint<BigDecimal>?,

    val nonRenewableEnergyProduction: DataPoint<BigDecimal>?,

    val highImpactClimateSectorEnergyConsumptionNaceA: DataPoint<BigDecimal>?,

    val highImpactClimateSectorEnergyConsumptionNaceB: DataPoint<BigDecimal>?,

    val highImpactClimateSectorEnergyConsumptionNaceC: DataPoint<BigDecimal>?,

    val highImpactClimateSectorEnergyConsumptionNaceD: DataPoint<BigDecimal>?,

    val highImpactClimateSectorEnergyConsumptionNaceE: DataPoint<BigDecimal>?,

    val highImpactClimateSectorEnergyConsumptionNaceF: DataPoint<BigDecimal>?,

    val highImpactClimateSectorEnergyConsumptionNaceG: DataPoint<BigDecimal>?,

    val highImpactClimateSectorEnergyConsumptionNaceH: DataPoint<BigDecimal>?,

    val highImpactClimateSectorEnergyConsumptionNaceL: DataPoint<BigDecimal>?,

    val totalHighImpactClimateSectorEnergyConsumption: DataPoint<BigDecimal>?,

    val nonRenewableEnergyConsumptionFossilFuels: DataPoint<BigDecimal>?,

    val nonRenewableEnergyConsumptionCrudeOil: DataPoint<BigDecimal>?,

    val nonRenewableEnergyConsumptionNaturalGas: DataPoint<BigDecimal>?,

    val nonRenewableEnergyConsumptionLignite: DataPoint<BigDecimal>?,

    val nonRenewableEnergyConsumptionCoal: DataPoint<BigDecimal>?,

    val nonRenewableEnergyConsumptionNuclearEnergy: DataPoint<BigDecimal>?,

    val nonRenewableEnergyConsumptionOther: DataPoint<BigDecimal>?,
)
