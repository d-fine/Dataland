package org.dataland.datalandbackend.model.lksg

import org.dataland.datalandbackend.model.lksg.submodels.LksgCodeOfConduct
import org.dataland.datalandbackend.model.lksg.submodels.LksgEnvironment
import org.dataland.datalandbackend.model.lksg.submodels.LksgGovernanceOsh
import org.dataland.datalandbackend.model.lksg.submodels.LksgRiskManagement
import org.dataland.datalandbackend.model.lksg.submodels.LksgSocialAndEmployeeMatters

/**
 * --- API model ---
 * Impact topics of the LKSG questionnaire's impact area "Governance"
 */
data class LksgGovernance(
    val socialAndEmployeeMatters: LksgSocialAndEmployeeMatters?,
    val environment: LksgEnvironment?,
    val osh: LksgGovernanceOsh?,
    val riskManagement: LksgRiskManagement?,
    val codeOfConduct: LksgCodeOfConduct?,
)
