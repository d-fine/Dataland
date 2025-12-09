package org.dataland.datalandbackend.validator

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class DataTypeValidatorTest {
    private val dataTypeValidator = DataTypeValidator()

    @Test
    fun `should return true when dataType is null`() {
        assertTrue(dataTypeValidator.isValid(null, null))
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "sfdr",
            "eutaxonomy-financials",
            "eutaxonomy-non-financials",
            "nuclear-and-gas",
            "pcaf",
            "lksg",
            "vsme",
        ],
    )
    fun `should return true for valid data types`(dataType: String) {
        assertTrue(dataTypeValidator.isValid(dataType, null))
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "TYPE_X",
            "UNKNOWN",
            "",
            " ",
            "123",
            "!@#",
        ],
    )
    fun `should return false for invalid data types`(dataType: String) {
        assertFalse(dataTypeValidator.isValid(dataType, null))
    }
}
