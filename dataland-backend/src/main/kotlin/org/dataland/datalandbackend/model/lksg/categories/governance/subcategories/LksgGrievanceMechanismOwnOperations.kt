package org.dataland.datalandbackend.model.lksg.categories.governance.subcategories

import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Grievance mechanism - Own operations"
 */
data class LksgGrievanceMechanismOwnOperations(
    val grievanceHandlingMechanism: YesNo?,

    val grievanceHandlingMechanismUsedForReporting: YesNo?,

    val grievanceMechanismInformationProvided: YesNo?,

    val grievanceMechanismSupportProvided: YesNo?,

    val grievanceMechanismAccessToExpertise: YesNo?,

    val grievanceMechanismComplaints: YesNo?,

    val grievanceMechanismComplaintsNumber: BigDecimal?,

    val grievanceMechanismComplaintsReason: String?,

    val grievanceMechanismComplaintsAction: YesNo?,

    val grievanceMechanismComplaintsActionUndertaken: String?,

    val grievanceMechanismPublicAccess: YesNo?,

    val grievanceMechanismProtection: YesNo?,

    val grievanceMechanismDueDiligenceProcess: YesNo?,
)
