package org.dataland.datalandbackend.model.lksg.categories.general

import org.dataland.datalandbackend.model.lksg.categories.general.subcategories.LksgMasterData
import org.dataland.datalandbackend.model.lksg.categories.general.subcategories.LksgProductionSpecific

/**
 * --- API model ---
 * Impact topics of the LKSG questionnaire's impact area "General"
 */
data class LksgGeneral(
    val masterData: LksgMasterData?,

    val productionSpecific: LksgProductionSpecific?,
)
