package org.dataland.datalandbackend.model.lksg.submodels

import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Child labour"
 */
data class ChildLabour(
    val employeeUnder18: YesNo? = null,

    val employeeUnder18Under15: YesNo? = null,

    val employeeUnder18Apprentices: YesNo? = null,

    val employmentUnderLocalMinimumAgePrevention: YesNo? = null,

    val employmentUnderLocalMinimumAgePreventionEmploymentContracts: YesNo? = null,

    val employmentUnderLocalMinimumAgePreventionJobDescription: YesNo? = null,

    val employmentUnderLocalMinimumAgePreventionIdentityDocuments: YesNo? = null,

    val employmentUnderLocalMinimumAgePreventionTraining: YesNo? = null,

    val employmentUnderLocalMinimumAgePreventionCheckingOfLegalMinimumAge: YesNo? = null,
)
