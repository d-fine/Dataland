package org.dataland.datalandbackend.model.p2p.categories.livestockFarming.subcategories

import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Emissions from manure and fertiliser and livestock" belonging to the category "Livestock
 * farming" of the p2p framework.
*/
data class P2pLivestockFarmingEmissionsFromManureAndFertiliserAndLivestock(
    val compostedFermentedManureInPercent: BigDecimal? = null,
    val emissionProofFertiliserStorageInPercent: BigDecimal? = null,
)
