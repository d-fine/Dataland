package org.dataland.datalandbackend.model.lksg.categories.governance

import org.dataland.datalandbackend.model.lksg.categories.governance.subcategories
      .LksgGovernanceRiskManagementOwnOperations
import org.dataland.datalandbackend.model.lksg.categories.governance.subcategories
      .LksgGovernanceGrievanceMechanismOwnOperations
import org.dataland.datalandbackend.model.lksg.categories.governance.subcategories
      .LksgGovernanceCertificationsPoliciesAndResponsibilities
import org.dataland.datalandbackend.model.lksg.categories.governance.subcategories
      .LksgGovernanceGeneralViolations

/**
 * --- API model ---
 * Fields of the category "Governance" of the lksg framework.
*/
data class LksgGovernance(
      val riskManagementOwnOperations: LksgGovernanceRiskManagementOwnOperations? = null,

      val grievanceMechanismOwnOperations: LksgGovernanceGrievanceMechanismOwnOperations? = null,

      val certificationsPoliciesAndResponsibilities: LksgGovernanceCertificationsPoliciesAndResponsibilities? = null,

      val generalViolations: LksgGovernanceGeneralViolations? = null,
)
