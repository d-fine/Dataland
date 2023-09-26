package org.dataland.datalandbackend.model.p2p.categories.ammonia.subcategories

import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the P2P questionnaire regarding the defossilisation of ammonia production
 */
data class P2pAmmoniaDefossilisation(
    val useOfRenewableFeedstocksInPercent: BigDecimal? = null,
)
