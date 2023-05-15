package org.dataland.datalandbackend.model.lksg.categories.social.subcategories

import org.dataland.datalandbackend.model.BaseDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.enums.commons.YesNoNa

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Withholding adequate wages"
 */
data class LksgWithholdingAdequateWages(
        val adequateWage: BaseDataPoint<YesNo>?,

        val adequateWageBeingWithheld: BaseDataPoint<YesNo>?,

        val documentedWorkingHoursAndWages: BaseDataPoint<YesNo>?,

        val adequateLivingWage: BaseDataPoint<YesNo>?,

        val regularWagesProcessFlow: BaseDataPoint<YesNo>?,

        val fixedHourlyWages: BaseDataPoint<YesNoNa>?,

        val fixedPieceworkWages: BaseDataPoint<YesNoNa>?,

        val adequateWageMeasures: BaseDataPoint<String>?,
)
