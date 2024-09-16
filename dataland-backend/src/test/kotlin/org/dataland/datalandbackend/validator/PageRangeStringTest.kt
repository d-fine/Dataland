package org.dataland.datalandbackend.validator

import jakarta.validation.Validation
import jakarta.validation.Validator
import org.junit.jupiter.api.Test

class PageRangeStringTest {

    private val validator: Validator = Validation.buildDefaultValidatorFactory().validator

    private data class PageRangeHolder(
        @field:PageRange
        val pageRange: String,
    )

    @Test
    fun `check that valid page ranges are processed correctly`() {
        listOf("1", "5", "10", "1-2", "2-5", "4-10").forEach {
            val violations = validator.validate(PageRangeHolder(it))
            println("Testing value $it: Violations: ${violations.size}")
            assert(violations.isEmpty()) { "Expected no violations for valid input: $it" }
        }
    }

    @Test
    fun `check that validation fails correctly for invalid ranges and numbers`() {
        listOf("0", "01", "-1", "abc", "3-2", "5-5", "0-10", "4--2", "abc-def").forEach {
            val violations = validator.validate(PageRangeHolder(it))
            println("Testing value $it: Violations: ${violations.size}")
            assert(violations.size == 1) { "Expected 1 violation for invalid input: $it" }
        }
    }
}
