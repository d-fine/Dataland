package org.dataland.datalandbackend.model.p2p.categories.realEstate.subcategories

import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the P2P questionnaire regarding the building efficiency of the real estate sector
 */
data class P2pRealEstateBuildingEfficiency(
    val buildingSpecificReburbishmentRoadmap: BigDecimal? = null,

    val zeroEmissionBuildingShare: BigDecimal? = null,

    val buildingEnergyEfficiency: BigDecimal? = null,
)
