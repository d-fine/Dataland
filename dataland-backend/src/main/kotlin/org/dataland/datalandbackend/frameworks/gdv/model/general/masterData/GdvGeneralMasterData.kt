package org.dataland.datalandbackend.frameworks.gdv.model.general.masterData

import java.time.LocalDate
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * The data-model for the MasterData section
 */
data class GdvGeneralMasterData(
    val gueltigkeitsDatum: LocalDate,
    val berichtsPflicht: YesNo?,
)
