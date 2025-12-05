package org.dataland.datalandbackend.validator

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DataTypeValidatorTest {
    private val dataTypeValidator = DataTypeValidator()

    @Test
    fun `should return true when dataType is null`() {
        assertTrue(dataTypeValidator.isValid(null, null))
    }

    @Test
    fun `should return true for valid data types`() {
        assertTrue(dataTypeValidator.isValid("sfdr", null))
        assertTrue(dataTypeValidator.isValid("eutaxonomy-financials", null))
        assertTrue(dataTypeValidator.isValid("eutaxonomy-non-financials", null))
        assertTrue(dataTypeValidator.isValid("nuclear-and-gas", null))
        assertTrue(dataTypeValidator.isValid("pcaf", null))
        assertTrue(dataTypeValidator.isValid("lksg", null))
        assertTrue(dataTypeValidator.isValid("vsme", null))
    }

    @Test
    fun `should return false for invalid data types`() {
        assertFalse(dataTypeValidator.isValid("TYPE_X", null))
        assertFalse(dataTypeValidator.isValid("UNKNOWN", null))
        assertFalse(dataTypeValidator.isValid("", null))
    }

    @Test
    fun `should return false for whitespace and gibberish`() {
        assertFalse(dataTypeValidator.isValid(" ", null))
        assertFalse(dataTypeValidator.isValid("123", null))
        assertFalse(dataTypeValidator.isValid("!@#", null))
    }
}
