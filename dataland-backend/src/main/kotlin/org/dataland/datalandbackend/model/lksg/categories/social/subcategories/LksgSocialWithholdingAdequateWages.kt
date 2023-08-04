package org.dataland.datalandbackend.model.lksg.categories.social.subcategories

import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.enums.commons.YesNoNa

/**
 * --- API model ---
 * Fields of the subcategory "Withholding adequate wages" belonging to the category "Social" of the lksg framework.
*/
data class LksgSocialWithholdingAdequateWages(
    val adequateWage: YesNo? = null,

    val adequateWagesMeasures: YesNo? = null,

    val documentedWorkingHoursAndWages: YesNo? = null,

    val adequateLivingWage: YesNo? = null,

    val regularWagesProcessFlow: YesNo? = null,

    val fixedHourlyWages: YesNoNa? = null,

    val fixedPieceworkWages: YesNoNa? = null,

    val adequateWageMeasures: String? = null,
)
