package org.dataland.frameworktoolbox.template.components

import org.dataland.datalandspecification.specifications.CalculationRule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class FieldCalculationParserTest {
    @Test
    fun `null input returns null`() {
        assertNull(FieldCalculationParser.parse(null))
    }

    @Test
    fun `blank input returns null`() {
        assertNull(FieldCalculationParser.parse("   "))
    }

    @Test
    fun `parses a single sum rule`() {
        val input = "\"Sum\": [a;b]"
        val expected = listOf(CalculationRule(inputs = listOf("a", "b"), calculationMethod = "Sum"))
        assertEquals(expected, FieldCalculationParser.parse(input))
    }

    @Test
    fun `parses multiple rules separated by semi-colons`() {
        val input =
            "\"Sum\": [extendedDecimalScope1GhgEmissionsInTonnes;extendedDecimalScope2GhgEmissionsInTonnes]; " +
                "\"Division\": [example1,example2]"
        val expected =
            listOf(
                CalculationRule(
                    inputs =
                        listOf(
                            "extendedDecimalScope1GhgEmissionsInTonnes",
                            "extendedDecimalScope2GhgEmissionsInTonnes",
                        ),
                    calculationMethod = "Sum",
                ),
                CalculationRule(
                    inputs = listOf("example1", "example2"),
                    calculationMethod = "Division",
                ),
            )
        assertEquals(expected, FieldCalculationParser.parse(input))
    }

    @Test
    fun `accepts unquoted method names`() {
        val input = "Sum: [a;b]"
        val expected = listOf(CalculationRule(inputs = listOf("a", "b"), calculationMethod = "Sum"))
        assertEquals(expected, FieldCalculationParser.parse(input))
    }

    @Test
    fun `throws on malformed input`() {
        assertThrows<IllegalArgumentException> { FieldCalculationParser.parse("not a rule") }
    }

    @Test
    fun `throws on empty bracketed inputs`() {
        assertThrows<IllegalArgumentException> { FieldCalculationParser.parse("\"Sum\": []") }
    }
}
