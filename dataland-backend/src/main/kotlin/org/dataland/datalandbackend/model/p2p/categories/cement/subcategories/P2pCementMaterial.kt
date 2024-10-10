package org.dataland.datalandbackend.model.p2p.categories.cement.subcategories

import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Material" belonging to the category "Cement" of the p2p framework.
*/
data class P2pCementMaterial(
    val clinkerFactorReduction: BigDecimal? = null,
    val preCalcinedClayUsageInPercent: BigDecimal? = null,
    val circularEconomyContribution: YesNo? = null,
)
