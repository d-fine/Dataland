package org.dataland.datalandbackend.model.sme.categories.power.subcategories

import org.dataland.datalandbackend.model.enums.sme.PercentRangeForInvestmentsInEnergyEfficiency

/**
 * --- API model ---
 * Fields of the subcategory "Investments" belonging to the category "Power" of the sme framework.
*/
data class SmePowerInvestments(
    val percentageOfInvestmentsInEnhancingEnergyEfficiency: PercentRangeForInvestmentsInEnergyEfficiency? = null,
)
