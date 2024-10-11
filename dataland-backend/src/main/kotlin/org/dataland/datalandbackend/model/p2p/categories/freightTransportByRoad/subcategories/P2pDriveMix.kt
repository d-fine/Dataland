package org.dataland.datalandbackend.model.p2p.categories.freightTransportByRoad.subcategories

import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the P2p questionnaire regarding a single drive mix type
 */
data class P2pDriveMix(
    val driveMixPerFleetSegmentInPercent: BigDecimal?,
    val totalAmountOfVehicles: BigDecimal?,
)
