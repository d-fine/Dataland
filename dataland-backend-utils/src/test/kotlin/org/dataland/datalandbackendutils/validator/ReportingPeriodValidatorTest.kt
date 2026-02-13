package org.dataland.datalandbackendutils.validator

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ReportingPeriodValidatorTest {
    private val reportingPeriodValidator = ReportingPeriodValidator()

    @Test
    fun `should return true when reporting period is null`() {
        assertTrue(reportingPeriodValidator.isValid(null, null))
    }

    @Test
    fun `should return true for valid reporting period at start of range`() {
        assertTrue(reportingPeriodValidator.isValid("2010", null))
    }

    @Test
    fun `should return true for valid reporting period in middle of range`() {
        assertTrue(reportingPeriodValidator.isValid("2023", null))
    }

    @Test
    fun `should return true for valid reporting period at end of range`() {
        assertTrue(reportingPeriodValidator.isValid("2039", null))
    }

    @Test
    fun `should return false for year too early`() {
        assertFalse(reportingPeriodValidator.isValid("2009", null))
        assertFalse(reportingPeriodValidator.isValid("1999", null))
    }

    @Test
    fun `should return false for year too late`() {
        assertFalse(reportingPeriodValidator.isValid("2040", null))
        assertFalse(reportingPeriodValidator.isValid("2100", null))
    }

    @Test
    fun `should return false for non numeric`() {
        assertFalse(reportingPeriodValidator.isValid("abcd", null))
        assertFalse(reportingPeriodValidator.isValid("20XX", null))
    }

    @Test
    fun `should return false for short or long input`() {
        assertFalse(reportingPeriodValidator.isValid("200", null))
        assertFalse(reportingPeriodValidator.isValid("20234", null))
    }
}
