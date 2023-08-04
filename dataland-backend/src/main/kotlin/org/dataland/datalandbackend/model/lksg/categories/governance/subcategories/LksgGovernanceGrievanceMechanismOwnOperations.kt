package org.dataland.datalandbackend.model.lksg.categories.governance.subcategories

import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Grievance mechanism - Own Operations" belonging to the category "Governance" of the
 * lksg framework.
*/
data class LksgGovernanceGrievanceMechanismOwnOperations(
    val grievanceHandlingMechanism: YesNo? = null,

    val grievanceHandlingMechanismUsedForReporting: YesNo? = null,

    val grievanceMechanismInformationProvided: YesNo? = null,

    val grievanceMechanismSupportProvided: YesNo? = null,

    val grievanceMechanismAccessToExpertise: YesNo? = null,

    val grievanceMechanismComplaints: YesNo? = null,

    val grievanceMechanismComplaintsNumber: BigDecimal? = null,

    val grievanceMechanismComplaintsReason: String? = null,

    val grievanceMechanismComplaintsAction: YesNo? = null,

    val grievanceMechanismComplaintsActionUndertaken: String? = null,

    val grievanceMechanismPublicAccess: YesNo? = null,

    val grievanceMechanismProtection: YesNo? = null,

    val grievanceMechanismDueDiligenceProcess: YesNo? = null,
)
