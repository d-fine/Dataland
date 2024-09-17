package org.dataland.datalandbackend.model.p2p.categories.steel.subcategories

import java.math.BigDecimal
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model --- Fields of the subcategory "Energy" belonging to the category "Steel" of the p2p
 * framework.
 */
data class P2pSteelEnergy(
  val emissionIntensityOfElectricityInCorrespondingUnit: BigDecimal? = null,
  val greenHydrogenUsage: YesNo? = null,
)
