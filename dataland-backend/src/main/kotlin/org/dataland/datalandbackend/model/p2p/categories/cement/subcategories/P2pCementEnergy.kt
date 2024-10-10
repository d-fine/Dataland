package org.dataland.datalandbackend.model.p2p.categories.cement.subcategories

import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Energy" belonging to the category "Cement" of the p2p framework.
*/
data class P2pCementEnergy(
    val energyMixInPercent: BigDecimal? = null,
    val fuelMixInPercent: BigDecimal? = null,
    val thermalEnergyEfficiencyInPercent: BigDecimal? = null,
    val compositionOfThermalInputInPercent: BigDecimal? = null,
)
