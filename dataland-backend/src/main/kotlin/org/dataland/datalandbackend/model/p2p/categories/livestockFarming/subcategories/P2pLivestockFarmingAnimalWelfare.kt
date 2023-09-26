package org.dataland.datalandbackend.model.p2p.categories.livestockFarming.subcategories

import java.math.BigDecimal

/**
* --- API model ---
* Fields of the P2P questionnaire regarding the animalWelfare of the livestockFarming sector
*/
data class P2pLivestockFarmingAnimalWelfare(
    val mortalityRateInPercent: BigDecimal? = null,
)
