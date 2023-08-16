package org.dataland.datalandbackend.model.eutaxonomy.nonfinancials

import java.math.BigDecimal

/**
 * --- API model ---
 * This class describes an absolute amount of money with an associated currency
 */
data class MoneyAmount(
    val amount: BigDecimal?,
    val currency: String?,
)
