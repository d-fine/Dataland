package org.dataland.datalandbackend.model.eutaxonomy.nonfinancials

import java.math.BigDecimal

/**
 * --- API model ---
 * This class describes the relative and absolute share on a different financial asset
 */
data class RelativeAndAbsoluteFinancialShare(
    val relativeShareInPercent: BigDecimal?,
    val absoluteShare: AmountWithCurrency?,
)

// TODO is it necessary to give a currency here? The information here kind of depends on the total amount which already
//  has a currency.
