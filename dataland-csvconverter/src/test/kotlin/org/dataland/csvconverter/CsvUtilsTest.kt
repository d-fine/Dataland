package org.dataland.csvconverter

import org.dataland.csvconverter.csv.CsvUtils
import org.dataland.csvconverter.csv.CsvUtils.readCsvDecimal
import org.dataland.csvconverter.csv.CsvUtils.readCsvLong
import org.dataland.csvconverter.csv.CsvUtils.readCsvPercentage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CsvUtilsTest {
    private val testColumnName = "identity"
    private val testMapping = mapOf(testColumnName to testColumnName)

    private fun constructRow(identity: String): Map<String, String> {
        return mapOf(testColumnName to identity)
    }

    @Test
    fun `test that the percentage parsing works as expected`() {
        assertEquals(
            "0.0125".toBigDecimal().stripTrailingZeros(),
            testMapping.readCsvPercentage(testColumnName, constructRow("1,25%")),
        )

        assertEquals(
            "0.35".toBigDecimal().stripTrailingZeros(),
            testMapping.readCsvPercentage(testColumnName, constructRow("35%")),
        )

        assertEquals(
            "0.325".toBigDecimal().stripTrailingZeros(),
            testMapping.readCsvPercentage(testColumnName, constructRow("32,5 %")),
        )
    }

    @Test
    fun `test that the percentage parsing throws errors when invalid data has been supplied`() {
        assertThrows<IllegalArgumentException> {
            testMapping.readCsvPercentage(testColumnName, constructRow("1,25"))
        }
        assertThrows<IllegalArgumentException> {
            testMapping.readCsvPercentage(testColumnName, constructRow("1,25,335%"))
        }
        assertThrows<IllegalArgumentException> {
            testMapping.readCsvPercentage(testColumnName, constructRow("1234g4545,79"))
        }
    }

    @Test
    fun `test that the decimal parsing works as expected`() {
        assertEquals(
            "123332.456".toBigDecimal().stripTrailingZeros(),
            testMapping.readCsvDecimal(testColumnName, constructRow("123.332,456")),
        )

        assertEquals(
            "123400000".toBigDecimal().stripTrailingZeros(),
            testMapping.readCsvDecimal(testColumnName, constructRow("123,4 "), CsvUtils.SCALE_FACTOR_ONE_MILLION),
        )
    }

    @Test
    fun `test that the decimal parsing throws errors when invalid data has been supplied`() {
        assertThrows<IllegalArgumentException> {
            testMapping.readCsvPercentage(testColumnName, constructRow("1234g4545,79"))
        }
        assertThrows<IllegalArgumentException> {
            testMapping.readCsvPercentage(testColumnName, constructRow("1234,4545,79"))
        }
    }

    @Test
    fun `test that the long parsing works as expected`() {
        assertEquals(
            12345L,
            testMapping.readCsvLong(testColumnName, constructRow("12345")),
        )
        assertEquals(
            0L,
            testMapping.readCsvLong(testColumnName, constructRow("0")),
        )
    }

    @Test
    fun `test that the long parsing throws errors when invalid data has been supplied`() {
        assertThrows<IllegalArgumentException> {
            testMapping.readCsvLong(testColumnName, constructRow("1234g4545"))
        }
        assertThrows<IllegalArgumentException> {
            testMapping.readCsvLong(testColumnName, constructRow("1234,45"))
        }
    }
}
