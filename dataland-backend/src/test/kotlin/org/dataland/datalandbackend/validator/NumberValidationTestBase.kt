package org.dataland.datalandbackend.validator

import jakarta.validation.Validation
import org.dataland.datalandbackend.interfaces.datapoints.BaseDataPoint
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.enums.data.QualityOptions
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.math.BigInteger

abstract class NumberValidationTestBase {
    protected val validator = Validation.buildDefaultValidatorFactory().validator

    protected fun <T> dataPoint(value: T) = ExtendedDataPoint(value, QualityOptions.Reported)

    protected fun doForAllValidNumberTypes(
        value: Number,
        function: (Number) -> Unit,
    ) {
        function(value.toLong())
        function(BigDecimal.valueOf(value.toDouble()))
        function(BigInteger.valueOf(value.toLong()))
    }

    protected fun doForAllInvalidNumberTypes(
        value: Number,
        function: (Number) -> Unit,
    ) {
        function(value.toInt())
        function(value.toDouble())
        function(value.toFloat())
    }

    protected fun assertNonDataPointExceptionThrown(value: Any?) {
        val exception =
            assertThrows<InvalidInputApiException> {
                validate(value)
            }
        Assertions.assertTrue(exception.message.contains("is not handled by number validator"))
        Assertions.assertFalse(exception.message.contains("data point"))
    }

    protected fun assertDataPointExceptionThrown(dataPoint: BaseDataPoint<*>) {
        val exception =
            assertThrows<InvalidInputApiException> {
                validate(dataPoint)
            }
        Assertions.assertTrue(exception.message.contains("as data point value is not handled by number validator"))
    }

    protected fun assertNoViolations(value: Any?) {
        assertNumberOfViolations(value, 0)
    }

    protected fun assertNumberOfViolations(
        value: Any?,
        expectedNumberOfViolations: Int,
    ) {
        val numValidations = validate(value)
        Assertions.assertEquals(expectedNumberOfViolations, numValidations)
    }

    protected abstract fun validate(value: Any?): Int
}
