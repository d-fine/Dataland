package org.dataland.datalandbackend.model.lksg

import org.dataland.datalandbackend.model.lksg.submodels.CodeOfConduct
import org.dataland.datalandbackend.model.lksg.submodels.Environment
import org.dataland.datalandbackend.model.lksg.submodels.GovernanceOsh
import org.dataland.datalandbackend.model.lksg.submodels.LksgSocialAndEmployeeMatters
import org.dataland.datalandbackend.model.lksg.submodels.RiskManagement

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
