package org.dataland.datalandbackend.model.lksg

import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Grievance Mechanism"
 */
data class GrievanceMechanism(
    val grievanceHandlingMechanism: YesNo?,

    val grievanceHandlingMechanismUsedForReporting: YesNo?,

    val legalProceedings: YesNo?,
)