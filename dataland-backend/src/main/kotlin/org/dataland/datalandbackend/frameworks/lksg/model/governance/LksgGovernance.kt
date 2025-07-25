// <--WARNING--> THIS FILE IS AUTO-GENERATED BY THE FRAMEWORK-TOOLBOX AND WILL BE OVERWRITTEN
package org.dataland.datalandbackend.frameworks.lksg.model.governance

import jakarta.validation.Valid
import org.dataland.datalandbackend.frameworks.lksg.model.governance.certificationsPoliciesAndResponsibilities
    .LksgGovernanceCertificationsPoliciesAndResponsibilities
import org.dataland.datalandbackend.frameworks.lksg.model.governance.generalViolations.LksgGovernanceGeneralViolations
import org.dataland.datalandbackend.frameworks.lksg.model.governance.grievanceMechanismOwnOperations
    .LksgGovernanceGrievanceMechanismOwnOperations
import org.dataland.datalandbackend.frameworks.lksg.model.governance.riskManagementOwnOperations
    .LksgGovernanceRiskManagementOwnOperations

/**
 * The data-model for the Governance section
 */
@Suppress("MaxLineLength")
data class LksgGovernance(
    @field:Valid()
    val riskManagementOwnOperations: LksgGovernanceRiskManagementOwnOperations? = null,
    @field:Valid()
    val grievanceMechanismOwnOperations: LksgGovernanceGrievanceMechanismOwnOperations? = null,
    @field:Valid()
    val certificationsPoliciesAndResponsibilities: LksgGovernanceCertificationsPoliciesAndResponsibilities? = null,
    @field:Valid()
    val generalViolations: LksgGovernanceGeneralViolations? = null,
)
