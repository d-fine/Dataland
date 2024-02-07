package org.dataland.datalandbackend.model.eutaxonomy.nonfinancials

import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.custom.AmountWithCurrency
import java.math.BigDecimal

/**
 * --- API model ---
 * This class describes the relative and absolute share on a different financial asset
 */
data class RelativeAndAbsoluteFinancialShare(
    val relativeShareInPercent: BigDecimal?,
    val absoluteShare: AmountWithCurrency?,
)
