package org.dataland.datalandbackend.model.p2p.categories.realEstate.subcategories

import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the P2P questionnaire regarding the building efficiency of the real estate sector
 */
data class P2pRealEstateBuildingEfficiency(
    val buildingSpecificRefurbishmentRoadmapInPercent: BigDecimal? = null,
    val zeroEmissionBuildingShareInPercent: BigDecimal? = null,
    val buildingEnergyEfficiencyInCorrespondingUnit: BigDecimal? = null,
)
