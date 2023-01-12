package org.dataland.datalandbackend.model.lksg.submodels

import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Human rights"
 */
data class LksgHumanRights(
    val diversityAndInclusionRole: YesNo? = null,

    val preventionOfMistreatments: YesNo? = null,

    val equalOpportunitiesOfficer: YesNo? = null,

    val riskOfHarmfulPollution: YesNo? = null,

    val unlawfulEvictionAndTakingOfLand: YesNo? = null,

    val useOfPrivatePublicSecurityForces: YesNo? = null,

    val useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights: YesNo? = null,
)