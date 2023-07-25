package org.dataland.datalandbackend.model.sme.categories.power

import org.dataland.datalandbackend.model.sme.categories.power.subcategories.SmePowerConsumption
import org.dataland.datalandbackend.model.sme.categories.power.subcategories.SmePowerInvestments

/**
 * --- API model ---
 * Fields of the category "Power" of the sme framework.
*/
data class SmePower(
    val investments: SmePowerInvestments? = null,

    val consumption: SmePowerConsumption? = null,
)
