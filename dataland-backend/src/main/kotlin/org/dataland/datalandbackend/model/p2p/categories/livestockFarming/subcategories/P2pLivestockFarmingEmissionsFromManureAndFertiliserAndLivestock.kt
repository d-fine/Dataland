package org.dataland.datalandbackend.model.p2p.categories.livestockFarming.subcategories

import java.math.BigDecimal

/**
* --- API model ---
* Fields of the P2P questionnaire regarding the emissions from manure, fertiliser, and livestock
* of the livestock Farming sector
*/
data class P2pLivestockFarmingEmissionsFromManureAndFertiliserAndLivestock(
    val compostedFermentedManureInPercent: BigDecimal? = null,

    val emissionProofFertiliserStorageInPercent: BigDecimal? = null,
)
