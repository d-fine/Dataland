package org.dataland.datalandbackend.model.lksg

data class Governance(
    val socialAndEmployeeMatters: SocialAndEmployeeMatters,
    val environment: Environment,
    val osh: GovernanceOsh,
    val riskManagement: RiskManagement,
    val codeOfConduct: CodeOfConduct
)