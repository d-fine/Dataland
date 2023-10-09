package org.dataland.datalandbackend.model.sme.categories.power.subcategories

import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.enums.sme.EnergySourceForHeatingAndHotWater
import org.dataland.datalandbackend.model.enums.sme.PercentRangeForEnergyConsumptionCoveredByOwnRenewablePower
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Consumption" belonging to the category "Power" of the sme framework.
*/
data class SmePowerConsumption(
    val powerConsumptionInMWh: BigDecimal? = null,

    val powerFromRenewableSources: YesNo? = null,

    val energyConsumptionHeatingAndHotWaterInMWh: BigDecimal? = null,

    val primaryEnergySourceForHeatingAndHotWater: EnergySourceForHeatingAndHotWater? = null,

    val percentageRangeForEnergyConsumptionCoveredByOwnRenewablePowerGeneration:
    PercentRangeForEnergyConsumptionCoveredByOwnRenewablePower? = null,
)
