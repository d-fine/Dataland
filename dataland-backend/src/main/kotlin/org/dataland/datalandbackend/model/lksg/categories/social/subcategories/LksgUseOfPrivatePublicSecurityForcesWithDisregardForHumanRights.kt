package org.dataland.datalandbackend.model.lksg.categories.social.subcategories

import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.enums.commons.YesNoNa

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Use of private / public security forces with disregard
 * for human rights"
 */
data class LksgUseOfPrivatePublicSecurityForcesWithDisregardForHumanRights(
    val useOfPrivatePublicSecurityForces: YesNo?,

    val useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights: YesNo?,

    val instructionOfSecurityForces: YesNo?,

    val humanRightsTraining: YesNo?,

    val stateSecurityForces: YesNoNa?,

    val privateSecurityForces: YesNoNa?,

    val useOfPrivatePublicSecurityForcesMeasures: List<String>?,
)
