package org.dataland.datalandbackend.model.lksg

import org.dataland.datalandbackend.model.enums.commons.YesNo

data class HumanRights(
    val diversityAndInclusionRole: YesNo?,

    val preventionOfMistreatments: YesNo?,

    val equalOpportunitiesOfficer: YesNo?,

    val riskOfHarmfulPollution: YesNo?,

    val unlawfulEvictionAndTakingOfLand: YesNo?,

    val useOfPrivatePublicSecurityForces: YesNo?,

    val useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights: YesNo?,
)