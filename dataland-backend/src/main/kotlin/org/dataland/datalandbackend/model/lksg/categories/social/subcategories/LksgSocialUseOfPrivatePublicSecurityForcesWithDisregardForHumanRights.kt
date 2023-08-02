package org.dataland.datalandbackend.model.lksg.categories.social.subcategories

import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.enums.commons.YesNoNa

/**
 * --- API model ---
 * Fields of the subcategory "Use of private/public security forces with disregard for human rights" belonging to the category "Social" of the lksg framework.
*/
data class LksgSocialUseOfPrivatePublicSecurityForcesWithDisregardForHumanRights(
      val useOfPrivatePublicSecurityForces: YesNo? = null,

      val useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights: YesNo? = null,

      val instructionOfSecurityForces: YesNo? = null,

      val humanRightsTraining: YesNo? = null,

      val stateSecurityForces: YesNoNa? = null,

      val privateSecurityForces: YesNoNa? = null,

      val useOfPrivatePublicSecurityForcesMeasures: String? = null,
)
