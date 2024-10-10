package org.dataland.datalandbackend.model.p2p.categories.livestockFarming.subcategories

import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Energy" belonging to the category "Livestock farming" of the p2p framework.
*/
data class P2pLivestockFarmingEnergy(
    val renewableElectricityInPercent: BigDecimal? = null,
    val renewableHeatingInPercent: BigDecimal? = null,
    val electricGasPoweredMachineryVehicleInPercent: BigDecimal? = null,
)
