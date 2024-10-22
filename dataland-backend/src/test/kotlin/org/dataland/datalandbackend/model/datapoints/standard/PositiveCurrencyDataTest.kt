package org.dataland.datalandbackend.model.datapoints.standard

import jakarta.validation.Validation
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class PositiveCurrencyDataTest {
    private val validator = Validation.buildDefaultValidatorFactory().validator

    @Test
    fun `Check that validation passes for PositiveCurrencyDataPoint`() {
        val correctExample =
            PositiveCurrencyData(
                value = BigDecimal.valueOf(0.5),
                currency = "USD",
            )
        val validationResult = validator.validate(correctExample)
        assert(validationResult.isEmpty()) { "Validation failed for correct example" }
    }

    @Test
    fun `Check that validation fails for PositiveCurrencyDataPoint`() {
        val incorrectExample =
            PositiveCurrencyData(
                value = BigDecimal.valueOf(-0.5),
                currency = "USD",
            )
        val validationResult = validator.validate(incorrectExample)
        assert(validationResult.isNotEmpty()) { "Validation passed for incorrect example" }
        println(validationResult.toString())
    }
}
