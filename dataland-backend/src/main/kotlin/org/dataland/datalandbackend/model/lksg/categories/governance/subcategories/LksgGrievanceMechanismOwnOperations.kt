package org.dataland.datalandbackend.model.lksg.categories.governance.subcategories

import org.dataland.datalandbackend.model.BaseDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Grievance mechanism - Own operations"
 */
data class LksgGrievanceMechanismOwnOperations(
    val grievanceHandlingMechanism: BaseDataPoint<YesNo>?,

    val grievanceHandlingMechanismUsedForReporting: BaseDataPoint<YesNo>?,

    val grievanceMechanismInformationProvided: BaseDataPoint<YesNo>?,

    val grievanceMechanismSupportProvided: BaseDataPoint<YesNo>?,

    val grievanceMechanismAccessToExpertise: BaseDataPoint<YesNo>?,

    val grievanceMechanismComplaints: BaseDataPoint<YesNo>?,

    val grievanceMechanismComplaintsNumber: BaseDataPoint<BigDecimal>?,

    val grievanceMechanismComplaintsReason: BaseDataPoint<String>?,

    val grievanceMechanismComplaintsAction: BaseDataPoint<YesNo>?,

    val grievanceMechanismComplaintsActionUndertaken: BaseDataPoint<String>?,

    val grievanceMechanismPublicAccess: BaseDataPoint<YesNo>?,

    val grievanceMechanismProtection: BaseDataPoint<YesNo>?,

    val grievanceMechanismDueDiligenceProcess: BaseDataPoint<YesNo>?,
)
