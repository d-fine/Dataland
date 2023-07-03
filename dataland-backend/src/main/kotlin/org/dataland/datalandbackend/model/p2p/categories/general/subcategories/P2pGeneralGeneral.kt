package org.dataland.datalandbackend.model.p2p.categories.general.subcategories

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.enums.p2p.P2pSector
import java.time.LocalDate
import java.util.*

/**
 * --- API model ---
 * Fields of the P2P questionnaire regarding top-level general information
 */
data class P2pGeneralGeneral(
    @field:JsonProperty(required = true)
    val dataDate: LocalDate,

    @field:JsonProperty(required = true)
    val sector: EnumSet<P2pSector>,
)
