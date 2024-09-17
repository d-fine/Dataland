package org.dataland.datalandbackend.model.p2p.categories.hvcPlastics.subcategories

import java.math.BigDecimal
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model --- Fields of the subcategory "Recycling" belonging to the category "HVC Plastics"
 * of the p2p framework.
 */
data class P2pHvcPlasticsRecycling(
  val contributionToCircularEconomy: YesNo? = null,
  val materialRecyclingInPercent: BigDecimal? = null,
  val chemicalRecyclingInPercent: BigDecimal? = null,
)
