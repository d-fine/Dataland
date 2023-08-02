package org.dataland.datalandbackend.model.lksg.categories.social.subcategories

import org.dataland.datalandbackend.model.BaseDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the subcategory "Child labor" belonging to the category "Social" of the lksg framework.
*/
data class LksgSocialChildLabor(
      val childLaborPreventionPolicy: BaseDataPoint<YesNo>? = null,

      val employeeSUnder18: YesNo? = null,

      val employeeSUnder15: YesNo? = null,

      val employeeSUnder18InApprenticeship: YesNo? = null,

      val worstFormsOfChildLaborProhibition: YesNo? = null,

      val worstFormsOfChildLabor: YesNo? = null,

      val worstFormsOfChildLaborForms: String? = null,

      val measuresForPreventionOfEmploymentUnderLocalMinimumAge: YesNo? = null,

      val employmentUnderLocalMinimumAgePreventionEmploymentContracts: YesNo? = null,

      val employmentUnderLocalMinimumAgePreventionJobDescription: YesNo? = null,

      val employmentUnderLocalMinimumAgePreventionIdentityDocuments: YesNo? = null,

      val employmentUnderLocalMinimumAgePreventionTraining: YesNo? = null,

      val employmentUnderLocalMinimumAgePreventionCheckingOfLegalMinimumAge: YesNo? = null,

      val additionalChildLaborMeasures: String? = null,
)
