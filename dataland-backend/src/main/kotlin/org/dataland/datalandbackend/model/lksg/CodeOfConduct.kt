package org.dataland.datalandbackend.model.lksg

import org.dataland.datalandbackend.model.enums.commons.YesNo

data class CodeOfConduct(
    val codeOfConduct: YesNo?,

    val codeOfConductRiskManagementTopics: YesNo?,

    val codeOfConductTraining: YesNo?,
)