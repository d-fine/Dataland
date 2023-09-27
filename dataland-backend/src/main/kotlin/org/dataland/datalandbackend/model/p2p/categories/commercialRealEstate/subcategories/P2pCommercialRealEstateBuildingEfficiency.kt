package org.dataland.datalandbackend.model.p2p.categories.commercialRealEstate.subcategories

import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Building efficiency" belonging to the category "Commercial Real Estate" of the p2p
 * framework.
*/
data class P2pCommercialRealEstateBuildingEfficiency(
    val buildingSpecificRefurbishmentRoadmapInPercent: BigDecimal? = null,

    val zeroEmissionBuildingShareInPercent: BigDecimal? = null,

    val buildingEnergyEfficiencyInCorrespondingUnit: BigDecimal? = null,
)
