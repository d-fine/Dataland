package org.dataland.datalandbackend.model.p2p.categories.residentialRealEstate.subcategories

import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Building efficiency" belonging to the category "Residential Real Estate" of the p2p
 * framework.
*/
data class P2pResidentialRealEstateBuildingEfficiency(
    val buildingSpecificRefurbishmentRoadmapInPercent: BigDecimal? = null,

    val zeroEmissionBuildingShareInPercent: BigDecimal? = null,

    val buildingEnergyEfficiencyInCorrespondingUnit: BigDecimal? = null,
)
