package org.dataland.datalandbackend.model.datapoints.standard

import org.dataland.datalandbackend.validator.Iso4217Currency
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of a currency data point without restrictions on the value
 */
data class CurrencyData(
    val value: BigDecimal,
    @field:Iso4217Currency
    val currency: String,
)
