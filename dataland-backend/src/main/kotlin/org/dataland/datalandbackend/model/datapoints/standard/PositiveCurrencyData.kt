package org.dataland.datalandbackend.model.datapoints.standard

import jakarta.validation.constraints.Min
import org.dataland.datalandbackend.validator.Iso4217Currency
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of a currency data point allowing only positive values
 */
data class PositiveCurrencyData(
    @field:Min(0)
    val value: BigDecimal,
    @field:Iso4217Currency
    val currency: String,
)
