package org.dataland.datalandbackend.model.eutaxonomy.nonfinancials

import java.math.BigDecimal

/**
 * --- API model ---
 * This class describes the relative and absolute share on a different financial asset
 */
data class FinancialShare(
    val percentage: BigDecimal?,
    val absoluteShare: MoneyAmount?,
)
