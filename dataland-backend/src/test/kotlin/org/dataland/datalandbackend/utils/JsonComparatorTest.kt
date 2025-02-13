package org.dataland.datalandbackend.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class JsonComparatorTest {
    private fun compareJsonStrings(
        expected: String,
        actual: String,
        options: JsonComparator.JsonComparisonOptions = JsonComparator.JsonComparisonOptions(),
    ): List<JsonComparator.JsonDiff> {
        val expectedJson = JsonTestUtils.testObjectMapper.readTree(expected)
        val actualJson = JsonTestUtils.testObjectMapper.readTree(actual)
        return JsonComparator.compareJson(expectedJson, actualJson, options)
    }

    @ParameterizedTest
    @CsvSource(
        delimiter = ';',
        value = [
            """{"a": null};{"a": {}}""",
            """{"a": {}};{"a": null}""",
            """{"a": null};{"a": {"c": null}}""",
        ],
    )
    fun `should see fully null objects and null as equal iff the option is enabled`(
        expected: String,
        actual: String,
    ) {
        for (fullyNullObjectsAreEqualToNull in listOf(true, false)) {
            val differences =
                compareJsonStrings(
                    expected, actual,
                    JsonComparator.JsonComparisonOptions(
                        fullyNullObjectsAreEqualToNull = fullyNullObjectsAreEqualToNull,
                    ),
                )

            assertEquals(if (fullyNullObjectsAreEqualToNull) 0 else 1, differences.size)
        }
    }

    @Test
    fun `should detect differences in simple JSON`() {
        val expected = """{"equal": "hello", "diff": 30}"""
        val actual = """{"equal": "hello", "diff": 31}"""

        val differences = compareJsonStrings(expected, actual)

        assertEquals(1, differences.size)
        assertEquals("diff", differences[0].path)
        assertEquals("30", differences[0].expected.toString())
        assertEquals("31", differences[0].actual.toString())
    }

    @Test
    fun `should detect differences in nested JSON`() {
        val expected = """{"equal": "hello", "nested": {"equal": "a", "delta": "3"}}"""
        val actual = """{"equal": "hello", "nested": {"equal": "a", "delta": "5"}}"""

        val differences = compareJsonStrings(expected, actual)

        assertEquals(1, differences.size)
        assertEquals("nested.delta", differences[0].path)
        assertEquals("\"3\"", differences[0].expected.toString())
        assertEquals("\"5\"", differences[0].actual.toString())
    }

    @Test
    fun `should ignore specified keys`() {
        val expected = """{"equal": "hello", "nested": {"equal": "a", "delta": "3"}}"""
        val actual = """{"equal": "hello", "nested": {"equal": "a", "delta": "5"}}"""
        val ignoredKeys = setOf("delta")

        val differences =
            compareJsonStrings(
                expected, actual,
                JsonComparator.JsonComparisonOptions(
                    ignoredKeys = ignoredKeys,
                ),
            )

        assertEquals(0, differences.size)
    }

    @Test
    fun `should detect differences in arrays`() {
        val expected = """{"array": ["A", "B"]}"""
        val actual = """{"array": ["A", "C"]}"""

        val differences = compareJsonStrings(expected, actual)

        assertEquals(1, differences.size)
        assertEquals("array[1]", differences[0].path)
        assertEquals("\"B\"", differences[0].expected.toString())
        assertEquals("\"C\"", differences[0].actual.toString())
    }

    @Test
    fun `should detect differences in array size`() {
        val expected = """{"array": ["A", "B"]}"""
        val actual = """{"array": ["A"]}"""

        val differences = compareJsonStrings(expected, actual)

        assertEquals(1, differences.size)
        assertEquals("array[1]", differences[0].path)
        assertEquals("\"B\"", differences[0].expected.toString())
        assertEquals("null", differences[0].actual.toString())
    }

    @Test
    fun `should detect differences when one field is missing`() {
        val expected = """{"A": "B"}"""
        val actual = """{}"""

        val differences = compareJsonStrings(expected, actual)

        assertEquals(1, differences.size)
        assertEquals("A", differences[0].path)
        assertEquals("\"B\"", differences[0].expected.toString())
        assertEquals("null", differences[0].actual.toString())
    }

    @Test
    fun `should detect differences when extra field is present`() {
        val expected = """{}"""
        val actual = """{"field1": "value1", "field2": 1}"""

        val differences = compareJsonStrings(expected, actual)

        assertEquals(2, differences.size)
        assertEquals("field1", differences[0].path)
        assertEquals("null", differences[0].expected.toString())
        assertEquals("\"value1\"", differences[0].actual.toString())
    }
}
