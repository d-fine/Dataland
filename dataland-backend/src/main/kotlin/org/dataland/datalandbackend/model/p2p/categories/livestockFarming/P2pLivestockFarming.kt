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
    val emissionsFromManureAndFertiliserAndLivestock: P2pLivestockFarmingEmissionsFromManureAndFertiliserAndLivestock?,

    val animalWelfare: P2pLivestockFarmingAnimalWelfare?,

    val animalFeed: P2pLivestockFarmingAnimalFeed?,

    val energy: P2pLivestockFarmingEnergy?,
)
