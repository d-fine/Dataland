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

    @Test
    fun `check that valid comma-separated page lists are processed correctly`() {
        listOf(
            "4, 112",
            "4,112",
            "4 , 112",
            "1-2,4",
            "1,3-5,10",
            "2,5-7",
            "1,2,3",
        ).forEach {
            val violations = validator.validate(PageRangeHolder(it))
            println("Testing value $it: Violations: ${violations.size}")
            assert(violations.isEmpty()) { "Expected no violations for valid input: $it" }
        }
    }

    @Test
    fun `check that validation fails correctly for invalid page lists`() {
        listOf(
            "4,,5",
            "4-6,5-7",
            "10-12,4",
            "4,4",
            "4,4-6",
            "4 - 6",
            ",4",
            "4,",
            "4,5,",
        ).forEach {
            val violations = validator.validate(PageRangeHolder(it))
            println("Testing value $it: Violations: ${violations.size}")
            assert(violations.size == 1) { "Expected 1 violation for invalid input: $it" }
        }
    }
}
