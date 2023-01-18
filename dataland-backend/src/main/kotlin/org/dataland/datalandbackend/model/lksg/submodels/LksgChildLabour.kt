package org.dataland.datalandbackend.model.lksg.submodels

import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Child labour"
 */
data class LksgChildLabour(
    val employeeUnder18: YesNo?,

    val employeeUnder15: YesNo?,

    val employeeUnder18Apprentices: YesNo?,

    val employmentUnderLocalMinimumAgePrevention: YesNo?,

    val employmentUnderLocalMinimumAgePreventionEmploymentContracts: YesNo?,

    val employmentUnderLocalMinimumAgePreventionJobDescription: YesNo?,

    val employmentUnderLocalMinimumAgePreventionIdentityDocuments: YesNo?,

    val employmentUnderLocalMinimumAgePreventionTraining: YesNo?,

    val employmentUnderLocalMinimumAgePreventionCheckingOfLegalMinimumAge: YesNo?,
)
