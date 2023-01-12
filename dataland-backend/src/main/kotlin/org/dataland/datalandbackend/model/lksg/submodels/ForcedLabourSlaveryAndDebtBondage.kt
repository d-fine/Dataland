package org.dataland.datalandbackend.model.lksg.submodels

import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Forced labour, slavery and debt bondage"
 */
data class ForcedLabourSlaveryAndDebtBondage(
    val forcedLabourAndSlaveryPrevention: YesNo? = null,

    val forcedLabourAndSlaveryPreventionEmploymentContracts: YesNo? = null,

    val forcedLabourAndSlaveryPreventionIdentityDocuments: YesNo? = null,

    val forcedLabourAndSlaveryPreventionFreeMovement: YesNo? = null,

    val forcedLabourAndSlaveryPreventionProvisionSocialRoomsAndToilets: YesNo? = null,

    val forcedLabourAndSlaveryPreventionProvisionTraining: YesNo? = null,

    val documentedWorkingHoursAndWages: YesNo? = null,

    val adequateLivingWage: YesNo? = null,

    val regularWagesProcessFlow: YesNo? = null,

    val fixedHourlyWages: YesNo? = null,
)