package org.dataland.datalandbackend.model.lksg.categories.social.subcategories

import org.dataland.datalandbackend.model.datapoints.BaseDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the subcategory "Forced labor, slavery" belonging to the category "Social" of the lksg framework.
*/
data class LksgSocialForcedLaborSlavery(
    val forcedLaborAndSlaveryPrevention: YesNo? = null,

    val forcedLaborAndSlaveryPreventionPractices: String? = null,

    val forcedLaborPreventionPolicy: BaseDataPoint<YesNo>? = null,

    val forcedLaborAndSlaveryPreventionMeasures: YesNo? = null,

    val forcedLaborAndSlaveryPreventionEmploymentContracts: YesNo? = null,

    val forcedLaborAndSlaveryPreventionIdentityDocuments: YesNo? = null,

    val forcedLaborAndSlaveryPreventionFreeMovement: YesNo? = null,

    val forcedLaborAndSlaveryPreventionProvisionSocialRoomsAndToilets: YesNo? = null,

    val forcedLaborAndSlaveryPreventionTraining: YesNo? = null,

    val forcedLaborAndSlaveryPreventionMeasuresOther: String? = null,
)
