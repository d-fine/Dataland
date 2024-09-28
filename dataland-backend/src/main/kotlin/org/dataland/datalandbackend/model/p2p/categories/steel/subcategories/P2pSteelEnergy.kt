package org.dataland.datalandbackend.model.p2p.categories.steel.subcategories

import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Energy" belonging to the category "Steel" of the p2p framework.
*/
data class P2pSteelEnergy(
    val emissionIntensityOfElectricityInCorrespondingUnit: BigDecimal? = null,
    val greenHydrogenUsage: YesNo? = null,
)
