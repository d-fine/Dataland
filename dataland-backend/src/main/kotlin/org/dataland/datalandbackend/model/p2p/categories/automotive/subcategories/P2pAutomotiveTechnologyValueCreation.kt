package org.dataland.datalandbackend.model.p2p.categories.automotive.subcategories

import java.math.BigDecimal
import java.time.LocalDate
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model --- Fields of the subcategory "Technology/value creation" belonging to the category
 * "Automotive" of the p2p framework.
 */
data class P2pAutomotiveTechnologyValueCreation(
  val driveMixInPercent: BigDecimal? = null,
  val icAndHybridEnginePhaseOutDate: LocalDate? = null,
  val futureValueCreationStrategy: YesNo? = null,
)
