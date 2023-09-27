package org.dataland.datalandbackend.model.p2p.categories.commercialRealEstate

import org.dataland.datalandbackend.model.p2p.categories.commercialRealEstate.subcategories.P2pCommercialRealEstateBuildingEfficiency
import org.dataland.datalandbackend.model.p2p.categories.commercialRealEstate.subcategories.P2pCommercialRealEstateEnergySource
import org.dataland.datalandbackend.model.p2p.categories.commercialRealEstate.subcategories.P2pCommercialRealEstateTechnology

/**
 * --- API model ---
 * Fields of the category "Commercial Real Estate" of the p2p framework.
*/
data class P2pCommercialRealEstate(
    val buildingEfficiency: P2pCommercialRealEstateBuildingEfficiency? = null,

    val energySource: P2pCommercialRealEstateEnergySource? = null,

    val technology: P2pCommercialRealEstateTechnology? = null,
)
