package org.dataland.datalandbackend.validator

import org.junit.jupiter.api.Test

class PageRangeStringTest : NumberValidationTestBase() {

    private data class PageRangeHolder(
        @PageRange
        val pageRange: String,
    )

    @Test
    fun `check that valid page ranges are processed correctly`() {
        // Valid inputs
        listOf("1", "5", "10", "1-2", "2-5", "4-10").forEach {
            assertNoViolations(PageRangeHolder(it))
        }
    }

    @Test
    fun `check that validation fails correctly for invalid ranges and numbers`() {
        // Invalid inputs
        listOf("0", "01", "-1", "abc", "3-2", "5-5", "0-10", "4--2", "abc-def").forEach {
            assertNumberOfViolations(PageRangeHolder(it), 1)
        }
    }

    override fun validate(value: Any?): Int {
        return when (value) {
            is PageRangeHolder -> {
                validator.validate(value).size
            }
            else -> throw IllegalArgumentException("Argument was not a PageRangeHolder")
        }
    }
}
