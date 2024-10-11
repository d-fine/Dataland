package org.dataland.datalandbackend.model.p2p.categories.freightTransportByRoad.subcategories

import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackend.model.enums.p2p.DriveMixType
import org.dataland.datalandbackend.utils.JsonExampleFormattingConstants
import java.time.LocalDate

/**
 * --- API model ---
 * Fields of the subcategory "Technology" belonging to the category "Freight transport by road" of the p2p framework.
*/
data class P2pFreightTransportByRoadTechnology(
    @field:Schema(example = JsonExampleFormattingConstants.DRIVE_MIX_DEFAULT_VALUE)
    val driveMixPerFleetSegment: Map<DriveMixType, P2pDriveMix>? = null,
    val icePhaseOut: LocalDate? = null,
)
