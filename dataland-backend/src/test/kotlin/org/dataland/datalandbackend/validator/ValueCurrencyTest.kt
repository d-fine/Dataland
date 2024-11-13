package org.dataland.datalandbackend.validator

import jakarta.validation.Validation
import jakarta.validation.Validator
import org.dataland.datalandbackend.model.datapoints.standard.CurrencyDataPoint
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class ValueCurrencyTest {
    private val validator: Validator = Validation.buildDefaultValidatorFactory().validator

    @Test
    fun `check that expected combinations are processed correctly`() {
        listOf(Pair(BigDecimal.valueOf(1.1), "USD"), Pair(null, null)).forEach {
            val violations = validator.validate(CurrencyDataPoint(it.first, it.second))
            println("Testing value $it: Violations: ${violations.size}")
            assert(violations.isEmpty()) { "Expected no violations for valid input: $it" }
        }
    }

    @Test
    fun `check that validation fails for invalid combinations`() {
        listOf(Pair(BigDecimal.valueOf(1.1), null), Pair(null, "USD")).forEach {
            val violations = validator.validate(CurrencyDataPoint(it.first, it.second))
            println("Testing value $it: Violations: ${violations.size}")
            assert(violations.size == 1) { "Expected 1 violation for invalid input: $it" }
        }
    }
}
