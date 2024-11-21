package org.dataland.datalandbackend.model.p2p.categories.livestockFarming

import jakarta.validation.Valid
import org.dataland.datalandbackend.model.p2p.categories.livestockFarming.subcategories.P2pLivestockFarmingAnimalFeed
import org.dataland.datalandbackend.model.p2p.categories.livestockFarming.subcategories.P2pLivestockFarmingAnimalWelfare
import org.dataland.datalandbackend.model.p2p.categories
    .livestockFarming.subcategories.P2pLivestockFarmingEmissionsFromManureAndFertiliserAndLivestock
import org.dataland.datalandbackend.model.p2p.categories.livestockFarming.subcategories.P2pLivestockFarmingEnergy

/**
 * --- API model ---
 * Fields of the category "Livestock farming" of the p2p framework.
*/
data class P2pLivestockFarming(
    val emissionsFromManureAndFertiliserAndLivestock: P2pLivestockFarmingEmissionsFromManureAndFertiliserAndLivestock? = null,
    val animalWelfare: P2pLivestockFarmingAnimalWelfare? = null,
    @field:Valid
    val animalFeed: P2pLivestockFarmingAnimalFeed? = null,
    val energy: P2pLivestockFarmingEnergy? = null,
)
