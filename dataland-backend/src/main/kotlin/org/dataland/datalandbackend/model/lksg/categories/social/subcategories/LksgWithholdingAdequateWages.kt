package org.dataland.datalandbackend.model.lksg.categories.social.subcategories

import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.enums.commons.YesNoNa

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Withholding adequate wages"
 */
data class LksgWithholdingAdequateWages(
    val adequatWage: YesNo?,

    val adequatWageBeingWithheld: YesNo?,

    val documentedWorkingHoursAndWages: YesNo?,

    val adequateLivingWage: YesNo?,

    val regularWagesProcessFlow: YesNo?,

    val fixedHourlyWages: YesNoNa?,

    val fixedPieceworkWages: YesNoNa?,

    val adequateWageMeasures: String?,
)
