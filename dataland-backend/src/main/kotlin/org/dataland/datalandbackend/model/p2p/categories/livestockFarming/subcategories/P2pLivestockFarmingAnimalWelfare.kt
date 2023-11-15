package org.dataland.datalandbackend.model.p2p.categories.livestockFarming.subcategories

import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Animal welfare" belonging to the category "Livestock farming" of the p2p framework.
*/
data class P2pLivestockFarmingAnimalWelfare(
    val mortalityRateInPercent: BigDecimal? = null,
)
