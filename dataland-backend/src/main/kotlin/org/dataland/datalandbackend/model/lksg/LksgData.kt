package org.dataland.datalandbackend.model.lksg

import org.dataland.datalandbackend.annotations.DataType

/**
 * --- API model ---
 * Fields of the questionnaire for the LKSG framework
 */
@DataType("lksg")
data class LksgData(
    val social: LksgSocialData? = null,
    val governance: LksgGovernanceData? = null,
    val environmental: LksgEnvironmentalData? = null,
)
