package org.dataland.datalandbackend.model.lksg

import org.dataland.datalandbackend.annotations.DataType

/**
 * --- API model ---
 * Fields of the questionnaire for the LKSG framework
 */
@DataType("lksg")
data class LksgData(
    val social: Social,
    val governance: Governance,
    val environmental: Environmental,
)
