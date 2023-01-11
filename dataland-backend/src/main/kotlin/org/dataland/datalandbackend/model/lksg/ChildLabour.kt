package org.dataland.datalandbackend.model.lksg

import org.dataland.datalandbackend.model.enums.commons.YesNo

data class ChildLabour(
    val employeeUnder18: YesNo?,

    val employeeUnder18Under15: YesNo?,

    val employeeUnder18Apprentices: YesNo?,

    val employmentUnderLocalMinimumAgePrevention: YesNo?,

    val employmentUnderLocalMinimumAgePreventionEmploymentContracts: YesNo?,

    val employmentUnderLocalMinimumAgePreventionJobDescription: YesNo?,

    val employmentUnderLocalMinimumAgePreventionIdentityDocuments: YesNo?,

    val employmentUnderLocalMinimumAgePreventionTraining: YesNo?,

    val employmentUnderLocalMinimumAgePreventionCheckingOfLegalMinimumAge: YesNo?,
)