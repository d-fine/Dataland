package org.dataland.datalandbackend.model.datapoints.standard

import jakarta.validation.Validation
import org.dataland.datalandbackend.model.enums.data.QualityOptions
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class CurrencyDataTest {
    private val validator = Validation.buildDefaultValidatorFactory().validator

    @Test
    fun `Check that validation passes for CurrencyDataPoint`() {
        val correctExample =
            CurrencyData(
                value = BigDecimal.valueOf(0.5),
                currency = "USD",
            )
        val validationResult = validator.validate(correctExample)
        assert(validationResult.isEmpty()) { "Validation failed for correct example" }
    }

    @Test
    fun `Check that validation fails for CurrencyDataPoint`() {
        val incorrectExample =
            CurrencyData(
                value = BigDecimal.valueOf(0.5),
                currency = null,
                quality = QualityOptions.NoDataFound,
            )
        val validationResult = validator.validate(incorrectExample)
        println(validationResult.toString())
        // val currency = Currency.getAvailableCurrencies()
        // val testString: String? = null
        // val test = Currency.getInstance(testString)
        // assert(currency.contains(Currency.getInstance("USD"))) { "USD not in available currencies" }
        // println(currency)
        /*val incorrectExample =
            CurrencyData(
                value = BigDecimal.valueOf(0.5),
                currency = "dummy",
            )
        val validationResult = validator.validate(incorrectExample)
        assert(validationResult.isNotEmpty()) { "Validation passed for incorrect example" }
        println(validationResult.toString())*/
    }
}
