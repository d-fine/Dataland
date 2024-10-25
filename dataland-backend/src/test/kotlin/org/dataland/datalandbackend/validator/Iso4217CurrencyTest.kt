package org.dataland.datalandbackend.validator

import jakarta.validation.Validation
import jakarta.validation.Validator
import org.junit.jupiter.api.Test

class Iso4217CurrencyTest {
    private val validator: Validator = Validation.buildDefaultValidatorFactory().validator

    private data class CurrencyHolder(
        @field:Iso4217Currency
        val currency: String?,
    )

    @Test
    fun `check that valid currencies are processed correctly`() {
        listOf("USD", "EUR", "JPY", null).forEach {
            val violations = validator.validate(CurrencyHolder(it))
            println("Testing value $it: Violations: ${violations.size}")
            assert(violations.isEmpty()) { "Expected no violations for valid input: $it" }
        }
    }

    @Test
    fun `check that validation fails correctly for invalid inputs`() {
        listOf("dummy", "", "test", "USDJPY").forEach {
            val violations = validator.validate(CurrencyHolder(it))
            println("Testing value $it: Violations: ${violations.size}")
            assert(violations.size == 1) { "Expected 1 violation for invalid input: $it" }
        }
    }
}
