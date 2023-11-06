package org.dataland.frameworktoolbox.intermediate.components

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import java.math.BigDecimal

/**
 * A PercentageComponent represents a decimal percentage between 0 % and 100 %.
 */
class PercentageComponent(
    identifier: String,
    parent: FieldNodeParent,
) : DecimalComponent(identifier, parent) {
    override var constantUnitSuffix: String?
        get() = "%"
        set(value) { throw IllegalAccessException("Cannot set suffix of a percentage field") }

    override var minimumValue: BigDecimal?
        get() = BigDecimal.ZERO
        set(value) { throw IllegalAccessException("Cannot set minimum value of a percentage field") }

    @Suppress("MagicNumber")
    override var maximumValue: BigDecimal?
        get() = BigDecimal.valueOf(100L)
        set(value) { throw IllegalAccessException("Cannot set maximum value of a percentage field") }
}
