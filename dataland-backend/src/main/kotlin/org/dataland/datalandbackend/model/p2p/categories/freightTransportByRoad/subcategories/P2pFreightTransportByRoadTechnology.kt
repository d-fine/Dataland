package org.dataland.datalandbackend.model.p2p.categories.freightTransportByRoad.subcategories

import java.math.BigDecimal
import java.time.LocalDate

/**
 * --- API model ---
 * Fields of the subcategory "Technology" belonging to the category "Freight transport by road" of the p2p framework.
*/
data class P2pFreightTransportByRoadTechnology(
    val driveMixPerFleetSegmentInPercent: BigDecimal? = null,

    val icePhaseOut: LocalDate? = null,
)
