package org.dataland.datalandbackend.model.lksg.submodels

import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Human rights"
 */
data class LksgHumanRights(
    val diversityAndInclusionRole: YesNo?,

    val preventionOfMistreatments: YesNo?,

    val equalOpportunitiesOfficer: YesNo?,

    val riskOfHarmfulPollution: YesNo?,

    val unlawfulEvictionAndTakingOfLand: YesNo?,

    val useOfPrivatePublicSecurityForces: YesNo?,

    val useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights: YesNo?,
)