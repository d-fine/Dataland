package org.dataland.datalandbackend.model.p2p.categories.hvcPlastics.subcategories

import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Decarbonisation" belonging to the category "HVC Plastics" of the p2p framework.
*/
data class P2pHvcPlasticsDecarbonisation(
    val energyMixInPercent: BigDecimal? = null,
    val electrificationInPercent: BigDecimal? = null,
)
