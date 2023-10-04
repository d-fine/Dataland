package org.dataland.datalandbackend.model.p2p.categories.freightTransportByRoad.subcategories

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the P2p questionnaire regarding a single drive mix type
 */
data class P2pDriveMix(
    @field:JsonProperty(required = true)
    val driveMixPerFleetSegmentInPercent: BigDecimal?,

    val totalAmountOfVehicles: BigDecimal?,
)