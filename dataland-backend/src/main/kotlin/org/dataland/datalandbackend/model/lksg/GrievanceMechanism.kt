package org.dataland.datalandbackend.model.lksg

import org.dataland.datalandbackend.model.enums.commons.YesNo

data class GrievanceMechanism(
    val grievanceHandlingMechanism: YesNo?,

    val grievanceHandlingMechanismUsedForReporting: YesNo?,

    val legalProceedings: YesNo?,
)