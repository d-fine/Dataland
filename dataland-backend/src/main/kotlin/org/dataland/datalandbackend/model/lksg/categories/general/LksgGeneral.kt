package org.dataland.datalandbackend.model.lksg.categories.general

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.lksg.categories.general.subcategories.LksgGeneralMasterData
import org.dataland.datalandbackend.model.lksg.categories.general.subcategories.LksgGeneralProductionSpecific
import org.dataland.datalandbackend.model.lksg.categories.general.subcategories
    .LksgGeneralProductionSpecificOwnOperations

/**
 * --- API model ---
 * Fields of the category "General" of the lksg framework.
*/
data class LksgGeneral(
    @field:JsonProperty(required = true)
    val masterData: LksgGeneralMasterData,

    val productionSpecific: LksgGeneralProductionSpecific? = null,

    val productionSpecificOwnOperations: LksgGeneralProductionSpecificOwnOperations? = null,
)
