package org.dataland.datalandbackend.model.datapoints

import jakarta.validation.Valid
import java.math.BigDecimal
import org.dataland.datalandbackend.validator.MaximumValue
import org.dataland.datalandbackend.validator.MinimumValue

/**
 * --- API model ---
 * Fields of a generic data point
 */
data class StandardPercentageValue(
    @field:MinimumValue(minimumValue = 0)
    @field:MaximumValue(maximumValue = 100)
    val value: BigDecimal?,
    val applicable: Boolean?,
    )
