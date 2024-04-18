package org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.custom

import org.dataland.datalandbackend.model.generics.AmountWithCurrency
import java.math.BigDecimal

/**
 * --- API model ---
 * This class describes the relative and absolute share on a different financial asset
 */
data class RelativeAndAbsoluteFinancialShare(
    val relativeShareInPercent: BigDecimal?,
    val absoluteShare: AmountWithCurrency?,
)
