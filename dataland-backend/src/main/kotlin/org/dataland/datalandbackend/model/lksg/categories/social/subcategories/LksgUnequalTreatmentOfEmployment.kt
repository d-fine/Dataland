package org.dataland.datalandbackend.model.lksg.categories.social.subcategories

import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Unequal treatment of employment"
 */
data class LksgUnequalTreatmentOfEmployment(
    val unequalTreatmentOfEmployment: YesNo?,

    val diversityAndInclusionRole: YesNo?,

    val preventionOfMistreatments: YesNo?,

    val equalOpportunitiesOfficer: YesNo?,

    val fairAndEthicalRecruitmentPolicy: YesNo?,

    val equalOpportunitiesAndNonDiscriminationPolicy: YesNo?,
)
