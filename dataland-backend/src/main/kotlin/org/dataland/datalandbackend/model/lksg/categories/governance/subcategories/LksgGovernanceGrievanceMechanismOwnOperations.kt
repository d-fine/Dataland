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

    val grievanceHandlingReportingAccessible: YesNo? = null,

    val appropriateGrievanceHandlingInformation: YesNo? = null,

    val appropriateGrievanceHandlingSupport: YesNo? = null,

    val accessToExpertiseForGrievanceHandling: YesNo? = null,

    val grievanceComplaints: YesNo? = null,

    val complaintsNumber: BigDecimal? = null,

    val complaintsReason: String? = null,

    val actionsForComplaintsUndertaken: YesNo? = null,

    val whichActionsForComplaintsUndertaken: String? = null,

    val publicAccessToGrievanceHandling: YesNo? = null,

    val whistleblowerProtection: YesNo? = null,

    val dueDiligenceProcessForGrievanceHandling: YesNo? = null,
)
