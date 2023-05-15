package org.dataland.datalandbackend.model.lksg

import org.dataland.datalandbackend.annotations.DataType
import org.dataland.datalandbackend.model.lksg.categories.environmental.LksgEnvironmental
import org.dataland.datalandbackend.model.lksg.categories.general.LksgGeneral
import org.dataland.datalandbackend.model.lksg.categories.governance.LksgGovernance
import org.dataland.datalandbackend.model.lksg.categories.social.LksgSocial

/**
 * --- API model ---
 * Fields of the questionnaire for the LKSG framework
 */
@DataType("lksg")
data class LksgData(
    val general: LksgGeneral?,
    val governance: LksgGovernance?,
    val social: LksgSocial?,
    val environmental: LksgEnvironmental?,
)
