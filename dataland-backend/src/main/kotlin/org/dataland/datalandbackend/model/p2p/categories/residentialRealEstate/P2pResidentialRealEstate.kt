package org.dataland.datalandbackend.model.p2p.categories.residentialRealEstate

import org.dataland.datalandbackend.model.p2p.categories.residentialRealEstate.subcategories.P2pResidentialRealEstateBuildingEfficiency
import org.dataland.datalandbackend.model.p2p.categories.residentialRealEstate.subcategories.P2pResidentialRealEstateEnergySource
import org.dataland.datalandbackend.model.p2p.categories.residentialRealEstate.subcategories.P2pResidentialRealEstateTechnology

/**
 * --- API model ---
 * Fields of the category "Residential Real Estate" of the p2p framework.
*/
data class P2pResidentialRealEstate(
    val buildingEfficiency: P2pResidentialRealEstateBuildingEfficiency? = null,

    val energySource: P2pResidentialRealEstateEnergySource? = null,

    val technology: P2pResidentialRealEstateTechnology? = null,
)
