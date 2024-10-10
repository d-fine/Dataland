package org.dataland.datalandbackend.model.p2p.categories.general.subcategories

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.enums.p2p.P2pSector
import java.time.LocalDate
import java.util.EnumSet

/**
 * --- API model ---
 * Fields of the subcategory "General" belonging to the category "General" of the p2p framework.
*/
data class P2pGeneralGeneral(
    @field:JsonProperty(required = true)
    val dataDate: LocalDate,
    @field:JsonProperty(required = true)
    val sectors: EnumSet<P2pSector>,
)
