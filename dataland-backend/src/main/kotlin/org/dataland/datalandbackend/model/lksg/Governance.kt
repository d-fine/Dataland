package org.dataland.datalandbackend.model.lksg

/**
 * --- API model ---
 * Impact topics of the LKSG questionnaire's impact area "Governance"
 */
data class Governance(
    val socialAndEmployeeMatters: SocialAndEmployeeMatters? = null,
    val environment: Environment? = null,
    val osh: GovernanceOsh? = null,
    val riskManagement: RiskManagement? = null,
    val codeOfConduct: CodeOfConduct? = null,
)