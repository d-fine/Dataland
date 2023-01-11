package org.dataland.datalandbackend.model.lksg

import org.dataland.datalandbackend.model.lksg.submodels.*

/**
 * --- API model ---
 * Impact topics of the LKSG questionnaire's impact area "Governance"
 */
data class LksgGovernanceData(
    val socialAndEmployeeMatters: LksgSocialAndEmployeeMatters? = null,
    val environment: Environment? = null,
    val osh: GovernanceOsh? = null,
    val riskManagement: RiskManagement? = null,
    val codeOfConduct: CodeOfConduct? = null,
)