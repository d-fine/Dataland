package org.dataland.datalandbackend.model.p2p.categories.steel.subcategories

import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Technology" belonging to the category "Steel" of the p2p framework.
*/
data class P2pSteelTechnology(
    val blastFurnacePhaseOutInPercent: BigDecimal? = null,
    val lowCarbonSteelScaleUpInPercent: BigDecimal? = null,
)
