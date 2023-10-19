package org.dataland.datalandbackend.model.p2p.categories.freightTransportByRoad.subcategories

import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Energy" belonging to the category "Freight transport by road" of the p2p framework.
*/
data class P2pFreightTransportByRoadEnergy(
    val fuelMixInPercent: BigDecimal? = null,
)
