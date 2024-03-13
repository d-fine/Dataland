package org.dataland.datalandbackend.validator

import jakarta.validation.Validation
import org.dataland.datalandbackend.interfaces.datapoints.BaseDataPoint
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.enums.data.QualityOptions
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.math.BigInteger

class MinimumValueTest {
    private val validator = Validation.buildDefaultValidatorFactory().validator

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

    private fun <T> dataPoint(value: T) = ExtendedDataPoint(value, QualityOptions.Reported)

    private fun doForAllValidNumberTypes(value: Number, function: (Number) -> Unit) {
        function(value.toLong())
        function(BigDecimal.valueOf(value.toDouble()))
        function(BigInteger.valueOf(value.toLong()))
    }

    private fun doForAllInvalidNumberTypes(value: Number, function: (Number) -> Unit) {
        function(value.toInt())
        function(value.toDouble())
        function(value.toFloat())
    }

    private fun assertNonDataPointExceptionThrown(value: Any?) {
        val exception = assertThrows<InvalidInputApiException> {
            validate(value)
        }
        assertTrue(exception.message.contains("is not handled by number validator"))
        assertFalse(exception.message.contains("data point"))
    }

    private fun assertDataPointExceptionThrown(dataPoint: BaseDataPoint<*>) {
        val exception = assertThrows<InvalidInputApiException> {
            validate(dataPoint)
        }
        assertTrue(exception.message.contains("as data point value is not handled by number validator"))
    }

    private fun assertNoViolations(value: Any?) {
        assertNumberOfViolations(value, 0)
    }

    private fun assertNumberOfViolations(value: Any?, expectedNumberOfViolations: Int) {
        val numValidations = validate(value)
        assertEquals(expectedNumberOfViolations, numValidations)
    }

    private fun validate(value: Any?): Int {
        val violations = validator.validate(
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
