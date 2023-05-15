package org.dataland.datalandbackend.model.lksg.categories.social.subcategories

import org.dataland.datalandbackend.model.BaseDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Unequal treatment of employment"
 */
data class LksgUnequalTreatmentOfEmployment(
        val unequalTreatmentOfEmployment: BaseDataPoint<YesNo>?,

        val diversityAndInclusionRole: BaseDataPoint<YesNo>?,

        val preventionOfMistreatments: BaseDataPoint<YesNo>?,

        val equalOpportunitiesOfficer: BaseDataPoint<YesNo>?,

        val fairAndEthicalRecruitmentPolicy: BaseDataPoint<YesNo>?,

        val equalOpportunitiesAndNonDiscriminationPolicy: BaseDataPoint<YesNo>?,
)
