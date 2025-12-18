package org.dataland.datalandbackend.validator

import jakarta.validation.Validation
import jakarta.validation.Validator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

private data class FiscalYearEndTestBean(
    @field:ValidFiscalYearEnd
    val fiscalYearEnd: String?,
)

class FiscalYearEndValidatorTest {
    private val validator: Validator = Validation.buildDefaultValidatorFactory().validator

    @ParameterizedTest
    @ValueSource(
        strings = [
            "01-Jan",
            "31-Jan",
            "28-Feb",
            "01-Mar",
            "31-Mar",
            "30-Apr",
            "30-Jun",
            "30-Sep",
            "31-Oct",
            "31-Dec",
            "10-Aug",
        ],
    )
    fun `valid fiscal year end values should pass validation`(validValue: String) {
        val violations = validator.validate(FiscalYearEndTestBean(validValue))
        assertEquals(
            0,
            violations.size,
            "Expected no violations for valid value: $validValue",
        )
    }

    @Test
    fun `null should pass validation`() {
        val nullViolations = validator.validate(FiscalYearEndTestBean(null))
        assertEquals(0, nullViolations.size, "Expected no violations for null value")
    }

    @Test
    fun `invalid format should fail validation`() {
        val invalidValues =
            listOf(
                "1-Jan",
                "01-jan",
                "01-JAN",
                "32-Jan",
                "00-Jan",
                "15-ABC",
                "15-September",
                "15/Jan",
                "15Jan",
                "",
            )

        invalidValues.forEach { value ->
            val violations = validator.validate(FiscalYearEndTestBean(value))
            assertEquals(
                1,
                violations.size,
                "Expected one violation for invalid value: $value",
            )
        }
    }

    @Test
    fun `invalid day for specific months should fail validation`() {
        val invalidPerMonth =
            listOf(
                "31-Apr",
                "31-Jun",
                "31-Sep",
                "31-Nov",
                "29-Feb",
                "30-Feb",
                "31-Feb",
            )

        invalidPerMonth.forEach { value ->
            val violations = validator.validate(FiscalYearEndTestBean(value))
            assertEquals(
                1,
                violations.size,
                "Expected one violation for invalid date: $value",
            )
        }
    }
}
