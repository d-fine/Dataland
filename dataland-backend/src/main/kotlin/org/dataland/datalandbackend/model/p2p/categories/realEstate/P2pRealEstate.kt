package org.dataland.datalandbackend.model.p2p.categories.realEstate

import org.dataland.datalandbackend.model.p2p.categories.realEstate.subcategories.P2pRealEstateBuildingEfficiency
import org.dataland.datalandbackend.model.p2p.categories.realEstate.subcategories.P2pRealEstateEnergySource
import org.dataland.datalandbackend.model.p2p.categories.realEstate.subcategories.P2plRealEstateTechnology

/**
 * --- API model ---
 * Fields of the P2P questionnaire regarding the real estate sectors
 */
data class P2pRealEstate(
    val buildingEfficiency: P2pRealEstateBuildingEfficiency? = null,
    val energySource: P2pRealEstateEnergySource? = null,
    val technology: P2plRealEstateTechnology? = null,
)
