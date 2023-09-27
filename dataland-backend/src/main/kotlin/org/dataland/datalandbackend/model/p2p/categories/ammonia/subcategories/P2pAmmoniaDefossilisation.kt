package org.dataland.datalandbackend.model.p2p.categories.ammonia.subcategories

import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Defossilisation" belonging to the category "Ammonia" of the p2p framework.
*/
data class P2pAmmoniaDefossilisation(
    val useOfRenewableFeedstocksInPercent: BigDecimal? = null,
)
