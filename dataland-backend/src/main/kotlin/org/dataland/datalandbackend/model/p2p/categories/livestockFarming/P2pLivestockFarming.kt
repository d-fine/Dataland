package org.dataland.datalandbackend.model.p2p.categories.livestockFarming

import org.dataland.datalandbackend.model.p2p.categories.livestockFarming.subcategories.P2pLivestockFarmingAnimalFeed
import org.dataland.datalandbackend.model.p2p.categories.livestockFarming.subcategories.P2pLivestockFarmingAnimalWelfare
import org.dataland.datalandbackend.model.p2p.categories.livestockFarming.subcategories.P2pLivestockFarmingEmissionsFromManureAndFertiliserAndLivestock
import org.dataland.datalandbackend.model.p2p.categories.livestockFarming.subcategories.P2pLivestockFarmingEnergy

/**
* --- API model ---
* Fields of the P2P questionnaire regarding the livestock farming sector
*/
data class P2pLivestockFarming(
    val emissionsFromManureAndFertiliserAndLivestock:
    P2pLivestockFarmingEmissionsFromManureAndFertiliserAndLivestock? = null,

    val animalWelfare: P2pLivestockFarmingAnimalWelfare? = null,

    val animalFeed: P2pLivestockFarmingAnimalFeed? = null,

    val energy: P2pLivestockFarmingEnergy? = null,
)
