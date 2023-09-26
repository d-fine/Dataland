package org.dataland.datalandbackend.model.p2p.categories.freightTransportByRoad.subcategories

import java.math.BigDecimal
import java.time.LocalDate

/**
* --- API model ---
* Fields of the P2P questionnaire regarding the technology of the freight transport by road sector
*/
data class P2pFreightTransportByRoadTechnology(
    val driveMixPerFleetSegment: BigDecimal? = null,

    val icePhaseOut: LocalDate? = null,
)
