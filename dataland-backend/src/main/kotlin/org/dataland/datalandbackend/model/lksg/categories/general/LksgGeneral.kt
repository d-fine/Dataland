package org.dataland.datalandbackend.model.lksg.categories.general

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.lksg.categories.general.subcategories.LksgMasterData
import org.dataland.datalandbackend.model.lksg.categories.general.subcategories.production.LksgProductionSpecific

/**
 * --- API model ---
 * Impact topics of the LKSG questionnaire's impact area "General"
 */
data class LksgGeneral(
    @field:JsonProperty(required = true)
    val masterData: LksgMasterData,

    val productionSpecific: LksgProductionSpecific?,
)
