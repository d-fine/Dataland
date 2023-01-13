package org.dataland.datalandbackend.model.lksg.submodels

import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Environment"
 */
data class LksgEnvironment(
    val responsibilitiesForTheEnvironment: YesNo? = null,
)
