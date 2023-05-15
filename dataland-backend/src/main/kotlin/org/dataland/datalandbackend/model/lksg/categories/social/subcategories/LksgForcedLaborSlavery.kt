package org.dataland.datalandbackend.model.lksg.categories.social.subcategories

import org.dataland.datalandbackend.model.BaseDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Forced labor, slavery"
 */
data class LksgForcedLaborSlavery(
        val forcedLaborAndSlaveryPrevention: BaseDataPoint<YesNo>?,

        val forcedLaborAndSlaveryPreventionPractices: BaseDataPoint<String>?,

        val forcedLaborAndSlaveryPreventionMeasures: BaseDataPoint<YesNo>?,

        val forcedLaborAndSlaveryPreventionEmploymentContracts: BaseDataPoint<YesNo>?,

        val forcedLaborAndSlaveryPreventionIdentityDocuments: BaseDataPoint<YesNo>?,

        val forcedLaborAndSlaveryPreventionFreeMovement: BaseDataPoint<YesNo>?,

        val forcedLaborAndSlaveryPreventionProvisionSocialRoomsAndToilets: BaseDataPoint<YesNo>?,

        val forcedLaborAndSlaveryPreventionTraining: BaseDataPoint<YesNo>?,

        val forcedLaborAndSlaveryPreventionMeasuresOther: BaseDataPoint<String>?,

        val forcedLaborPreventionPolicy: BaseDataPoint<YesNo>?,
)
