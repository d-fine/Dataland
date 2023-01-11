package org.dataland.datalandbackend.model.lksg

import org.dataland.datalandbackend.model.enums.commons.YesNo

data class ForcedLabourSlaveryAndDebtBondage(
    val forcedLabourAndSlaveryPrevention: YesNo?,

    val forcedLabourAndSlaveryPreventionEmploymentContracts: YesNo?,

    val forcedLabourAndSlaveryPreventionIdentityDocuments: YesNo?,

    val forcedLabourAndSlaveryPreventionFreeMovement: YesNo?,

    val forcedLabourAndSlaveryPreventionProvisionSocialRoomsAndToilets: YesNo?,

    val forcedLabourAndSlaveryPreventionProvisionTraining: YesNo?,

    val documentedWorkingHoursAndWages: YesNo?,

    val adequateLivingWage: YesNo?,

    val regularWagesProcessFlow: YesNo?,

    val fixedHourlyWages: YesNo?,
)