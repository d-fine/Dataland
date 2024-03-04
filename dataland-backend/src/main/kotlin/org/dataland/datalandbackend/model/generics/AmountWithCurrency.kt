package org.dataland.datalandbackend.model.generics

import java.math.BigDecimal

/**
 * --- API model ---
 * This class describes an absolute amount of money with an associated currency
 */
data class AmountWithCurrency(
    val amount: BigDecimal?,
    val currency: String?,
)
