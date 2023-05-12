package org.dataland.datalandbackend.model.lksg.categories.social.subcategories

import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Child labor"
 */
data class LksgChildLabor(
    val employeeUnder18: YesNo?,

    val employeeUnder18Under15: YesNo?,

    val employeeUnder18Apprentices: YesNo?,

    val worstFormsOfChildLaborProhibition: YesNo?,

    val worstFormsOfChildLaborForms: String?,

    val employmentUnderLocalMinimumAgePrevention: YesNo?,

    val employmentUnderLocalMinimumAgePreventionEmploymentContracts: YesNo?,

    val employmentUnderLocalMinimumAgePreventionJobDescription: YesNo?,

    val employmentUnderLocalMinimumAgePreventionIdentityDocuments: YesNo?,

    val employmentUnderLocalMinimumAgePreventionTraining: YesNo?,

    val employmentUnderLocalMinimumAgePreventionCheckingOfLegalMinimumAge: YesNo?,

    val childLaborMeasures: String?,

    val childLaborPreventionPolicy: YesNo?,
)
