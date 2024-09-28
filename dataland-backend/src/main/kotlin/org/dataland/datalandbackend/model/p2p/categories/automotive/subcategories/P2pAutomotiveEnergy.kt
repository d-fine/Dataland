package org.dataland.datalandbackend.model.p2p.categories.automotive.subcategories

import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Energy" belonging to the category "Automotive" of the p2p framework.
*/
data class P2pAutomotiveEnergy(
    val productionSiteEnergyConsumptionInMWh: BigDecimal? = null,
    val energyMixInPercent: BigDecimal? = null,
)
