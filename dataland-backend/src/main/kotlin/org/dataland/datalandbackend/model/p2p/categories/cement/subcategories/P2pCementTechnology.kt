package org.dataland.datalandbackend.model.p2p.categories.cement.subcategories

import java.math.BigDecimal
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model --- Fields of the subcategory "Technology" belonging to the category "Cement" of
 * the p2p framework.
 */
data class P2pCementTechnology(
  val carbonCaptureAndUseTechnologyUsage: YesNo? = null,
  val electrificationOfProcessHeatInPercent: BigDecimal? = null,
)
