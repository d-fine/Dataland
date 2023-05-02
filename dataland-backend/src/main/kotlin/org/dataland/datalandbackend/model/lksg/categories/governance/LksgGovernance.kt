package org.dataland.datalandbackend.model.lksg.categories.governance

import org.dataland.datalandbackend.model.lksg.categories.governance.subcategories.LksgGrievanceMechanismOwnOperations
import org.dataland.datalandbackend.model.lksg.categories.governance.subcategories.LksgRiskManagementOwnOperations
import org.dataland.datalandbackend.model.lksg.categories.governance.subcategories.LksgCertificationsPoliciesAndResponsibilities
import org.dataland.datalandbackend.model.lksg.categories.governance.subcategories.LksgGeneralViolations

/**
 * --- API model ---
 * Impact topics of the LKSG questionnaire's impact area "Governance"
 */
data class LksgGovernance(
    val certificationsPoliciesAndResponsibilities: LksgCertificationsPoliciesAndResponsibilities?,

    val generalViolations: LksgGeneralViolations?,

    val riskManagementOwnOperations: LksgRiskManagementOwnOperations?,

    val grievanceMechanismOwnOperations: LksgGrievanceMechanismOwnOperations?,
)
