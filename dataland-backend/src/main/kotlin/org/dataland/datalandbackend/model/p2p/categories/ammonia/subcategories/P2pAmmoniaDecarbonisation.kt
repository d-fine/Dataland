package org.dataland.datalandbackend.model.p2p.categories.ammonia.subcategories

import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Decarbonisation" belonging to the category "Ammonia" of the p2p framework.
*/
data class P2pAmmoniaDecarbonisation(
    val energyMixInPercent: BigDecimal? = null,
    val ccsTechnologyAdoptionInPercent: BigDecimal? = null,
    val electrificationInPercent: BigDecimal? = null,
)
