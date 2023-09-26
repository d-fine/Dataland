package org.dataland.datalandbackend.model.p2p.categories.freightTransportByRoad.subcategories

import java.math.BigDecimal

/**
* --- API model ---
* Fields of the P2P questionnaire regarding the energy of the freight transport by road sector
*/
data class P2pFreightTransportByRoadEnergy(
    val fuelMixInPercent: BigDecimal? = null,
)
