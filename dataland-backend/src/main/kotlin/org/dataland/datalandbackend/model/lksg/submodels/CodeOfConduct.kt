package org.dataland.datalandbackend.model.lksg.submodels

import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Code of conduct"
 */
data class CodeOfConduct(
    val codeOfConduct: YesNo?,

    val codeOfConductRiskManagementTopics: YesNo?,

    val codeOfConductTraining: YesNo?,
)