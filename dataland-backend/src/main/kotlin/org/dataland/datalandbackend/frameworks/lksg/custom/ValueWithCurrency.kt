package org.dataland.datalandbackend.frameworks.lksg.custom

import java.math.BigDecimal

/**
 * --- API model ---
 * This class describes an absolute value of money with an associated currency
 */
data class ValueWithCurrency(
    val value: BigDecimal?,
    val currency: String?,
)
