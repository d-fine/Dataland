package org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.custom

import java.math.BigDecimal
import org.dataland.datalandbackend.model.generics.AmountWithCurrency

/**
 * --- API model --- This class describes the relative and absolute share on a different financial
 * asset
 */
data class RelativeAndAbsoluteFinancialShare(
  val relativeShareInPercent: BigDecimal?,
  val absoluteShare: AmountWithCurrency?,
)
