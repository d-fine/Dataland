package org.dataland.datalandbackend.model.lksg.categories.social.subcategories

import org.dataland.datalandbackend.model.BaseDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.enums.commons.YesNoNa

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Use of private / public security forces with disregard
 * for human rights"
 */
data class LksgUseOfPrivatePublicSecurityForcesWithDisregardForHumanRights(
        val useOfPrivatePublicSecurityForces: BaseDataPoint<YesNo>?,

        val useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights: BaseDataPoint<YesNo>?,

        val instructionOfSecurityForces: BaseDataPoint<YesNo>?,

        val humanRightsTraining: BaseDataPoint<YesNo>?,

        val stateSecurityForces: BaseDataPoint<YesNoNa>?,

        val privateSecurityForces: BaseDataPoint<YesNoNa>?,

        val useOfPrivatePublicSecurityForcesMeasures: BaseDataPoint<String>?,
)
