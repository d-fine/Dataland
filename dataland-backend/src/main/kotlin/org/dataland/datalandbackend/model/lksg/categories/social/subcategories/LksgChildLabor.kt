package org.dataland.datalandbackend.model.lksg.categories.social.subcategories

import org.dataland.datalandbackend.model.BaseDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Child labor"
 */
data class LksgChildLabor(
    val employeeUnder18: BaseDataPoint<YesNo>?,

    val employeeUnder18Under15: BaseDataPoint<YesNo>?,

    val employeeUnder18Apprentices: BaseDataPoint<YesNo>?,

    val worstFormsOfChildLaborProhibition: BaseDataPoint<YesNo>?,

    val worstFormsOfChildLaborForms: BaseDataPoint<YesNo>?,

    val employmentUnderLocalMinimumAgePrevention: BaseDataPoint<YesNo>?,

    val employmentUnderLocalMinimumAgePreventionEmploymentContracts: BaseDataPoint<YesNo>?,

    val employmentUnderLocalMinimumAgePreventionJobDescription: BaseDataPoint<YesNo>?,

    val employmentUnderLocalMinimumAgePreventionIdentityDocuments: BaseDataPoint<YesNo>?,

    val employmentUnderLocalMinimumAgePreventionTraining: BaseDataPoint<YesNo>?,

    val employmentUnderLocalMinimumAgePreventionCheckingOfLegalMinimumAge: BaseDataPoint<YesNo>?,

    val childLaborMeasures: BaseDataPoint<String>?,

    val childLaborPreventionPolicy: BaseDataPoint<YesNo>?,
)
