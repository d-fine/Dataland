package org.dataland.datalandbackend.model.lksg.categories.social.subcategories

import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.BaseDataPoint

/**
 * --- API model ---
 * Fields of the subcategory "Unequal treatment of employment" belonging to the category "Social" of the lksg framework.
*/
data class LksgSocialUnequalTreatmentOfEmployment(
      val unequalTreatmentOfEmployment: YesNo? = null,

      val diversityAndInclusionRole: YesNo? = null,

      val preventionOfMistreatments: YesNo? = null,

      val equalOpportunitiesOfficer: YesNo? = null,

      val fairAndEthicalRecruitmentPolicy: BaseDataPoint<YesNo>? = null,

      val equalOpportunitiesAndNonDiscriminationPolicy: BaseDataPoint<YesNo>? = null,
)
