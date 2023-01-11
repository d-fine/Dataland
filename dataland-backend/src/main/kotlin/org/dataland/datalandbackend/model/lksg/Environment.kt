package org.dataland.datalandbackend.model.lksg

import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Environment"
 */
data class Environment(
    val responsibilitiesForTheEnvironment: YesNo?,
)