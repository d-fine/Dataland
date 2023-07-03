package org.dataland.datalandbackend.model.p2p.categories.freightTransportByRoad

import org.dataland.datalandbackend.model.p2p.categories
    .freightTransportByRoad.subcategories.P2pFreightTransportByRoadEnergy
import org.dataland.datalandbackend.model.p2p.categories
    .freightTransportByRoad.subcategories.P2pFreightTransportByRoadTechnology

/**
* --- API model ---
* Fields of the P2P questionnaire regarding the Freight Transport By Road sector
*/
data class P2pFreightTransportByRoad(
    val technology: P2pFreightTransportByRoadTechnology? = null,

    val energy: P2pFreightTransportByRoadEnergy? = null,
)
