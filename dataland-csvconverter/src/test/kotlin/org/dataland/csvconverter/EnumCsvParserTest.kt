package org.dataland.csvconverter

import org.dataland.csvconverter.csv.utils.EnumCsvParser
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class EnumCsvParserTest {
    private val myParser = EnumCsvParser(
        mapOf(
            "1" to "one",
            "kekse" to "cookies",
        ),
    )

    @Test
    fun `test EnumCsvParser parse function`() {
        Assertions.assertTrue(myParser.parse("Number", "1") == "one")
    }

    @Test
    fun `test EnumCsvParser parseAllowingNull function`() {
        Assertions.assertTrue(myParser.parseAllowingNull("Word", "kekse") == "cookies")
    }

    @Test
    fun `test EnumCsvParser parse with illegal argument`() {
        assertThrows<IllegalArgumentException> { myParser.parse("Kekse", "keine Kekse") }
    }

    @Test
    fun `test EnumCsvParser parse with no argument`() {
        assertThrows<IllegalArgumentException> { myParser.parse("Fehler", "") }
    }

    @Test
    fun `test EnumCsvParser parseAllowingNull with illegal argument`() {
        assertThrows<IllegalArgumentException> { myParser.parse("Kekse", "keine Kekse") }
    }

    @Test
    fun `test EnumCsvParser parseAllowingNull with no argument`() {
        Assertions.assertTrue(myParser.parseAllowingNull("", null) == null)
    }
}
