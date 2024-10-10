package org.dataland.datalandbackend.validator

import org.dataland.datalandbackend.interfaces.datapoints.BaseDataPoint
import org.junit.jupiter.api.Test

class MinimumValueTest : NumberValidationTestBase() {
    private data class NumberFieldHolder(
        @MinimumValue(0)
        val value: Number?,
    )

    private data class DataPointFieldHolder(
        @MinimumValue(0)
        val dataPoint: BaseDataPoint<*>,
    )

    @Test
    fun `check that the number types are processed correctly`() {
        doForAllInvalidNumberTypes(1) {
            assertNonDataPointExceptionThrown(it)
            assertNonDataPointExceptionThrown(dataPoint(it))
        }
        doForAllValidNumberTypes(1) {
            assertNoViolations(it)
            assertNoViolations(dataPoint(it))
        }
        assertNoViolations(null)
        assertNoViolations(dataPoint(null))
        assertDataPointExceptionThrown(dataPoint("string"))
    }

    @Test
    fun `check that the validation fails correctly`() {
        doForAllValidNumberTypes(0) {
            assertNoViolations(it)
            assertNoViolations(dataPoint(it))
        }
        doForAllValidNumberTypes(-1) {
            assertNumberOfViolations(it, 1)
            assertNumberOfViolations(dataPoint(it), 1)
        }
    }

    override fun validate(value: Any?): Int {
        val violations =
            validator.validate(
                when (value) {
                    null -> NumberFieldHolder(null)
                    is Number -> NumberFieldHolder(value)
                    is BaseDataPoint<*> -> DataPointFieldHolder(value)
                    else -> throw IllegalArgumentException("Argument was neither null nor Number nor BaseDataPoint")
                },
            )
        return violations.size
    }
}
