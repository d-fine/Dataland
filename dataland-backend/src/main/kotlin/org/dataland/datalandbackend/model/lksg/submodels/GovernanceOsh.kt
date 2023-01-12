package org.dataland.datalandbackend.model.lksg.submodels

import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "OSH" of the impact area "Governance"
 */
data class GovernanceOsh(
    val responsibilitiesForOccupationalSafety: YesNo? = null,
)