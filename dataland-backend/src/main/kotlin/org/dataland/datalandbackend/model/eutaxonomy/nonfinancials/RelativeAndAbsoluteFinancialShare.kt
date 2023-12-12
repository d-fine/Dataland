package org.dataland.datalandbackend.model.eutaxonomy.nonfinancials

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.dataland.datalandbackend.model.eutaxonomy.ConstantParameters
import java.math.BigDecimal

/**
 * --- API model ---
 * This class describes the relative and absolute share on a different financial asset
 */
data class RelativeAndAbsoluteFinancialShare(
    @Min(ConstantParameters.PERCENT_MINIMUM)
    @Max(ConstantParameters.PERCENT_MAXIMUM)
    val relativeShareInPercent: BigDecimal?,
    val absoluteShare: AmountWithCurrency?,
)
