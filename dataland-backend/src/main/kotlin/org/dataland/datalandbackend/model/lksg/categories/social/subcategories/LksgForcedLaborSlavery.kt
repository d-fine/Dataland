package org.dataland.datalandbackend.model.lksg.categories.social.subcategories

import org.dataland.datalandbackend.model.BaseDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Forced labor, slavery"
 */
data class LksgForcedLaborSlavery(
        val forcedLaborAndSlaveryPrevention: YesNo?,

        val forcedLaborAndSlaveryPreventionPractices: YesNo?,

        val forcedLaborAndSlaveryPreventionMeasures: YesNo?,

        val forcedLaborAndSlaveryPreventionEmploymentContracts: YesNo?,

        val forcedLaborAndSlaveryPreventionIdentityDocuments: YesNo?,

        val forcedLaborAndSlaveryPreventionFreeMovement: YesNo?,

        val forcedLaborAndSlaveryPreventionProvisionSocialRoomsAndToilets: YesNo?,

        val forcedLaborAndSlaveryPreventionTraining: YesNo?,

        val forcedLaborAndSlaveryPreventionMeasuresOther: String?,

        val forcedLaborPreventionPolicy: BaseDataPoint<YesNo>?,
)
