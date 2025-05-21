package org.dataland.datalandbackendutils.services.utils

import org.dataland.datalandbackendutils.utils.JsonComparator
import org.dataland.datalandbackendutils.utils.JsonUtils.testObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class JsonComparatorTest {
    @ParameterizedTest
    @CsvSource(
        delimiter = ';',
        value = [
            """{"a": null};{"a": {}}""",
            """{"a": {}};{"a": null}""",
            """{"a": null};{"a": {"c": null}}""",
            """{"a": null};{"a": {"b": null, "c": {}, "d": []}}""",
            """{"a": null};{"a": {"b": null, "c": {"d": null, "e": null, "f": {"g": null, "h": {}}}}}""",
        ],
    )
    fun `should see fully null objects and null as equal iff the option is enabled`(
        expected: String,
        actual: String,
    ) {
        for (fullyNullObjectsAreEqualToNull in listOf(true, false)) {
            val differences =
                JsonComparator.compareJsonStrings(
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

        val differences = JsonComparator.compareJsonStrings(expected, actual)

        assertEquals(1, differences.size)
        assertEquals("diff", differences[0].path)
        assertEquals("30", differences[0].expected.toString())
        assertEquals("31", differences[0].actual.toString())
    }

    @Test
    fun `should detect differences in nested JSON`() {
        val expected = """{"equal": "hello", "nested": {"equal": "a", "delta": "3"}}"""
        val actual = """{"equal": "hello", "nested": {"equal": "a", "delta": "5"}}"""

        val differences = JsonComparator.compareJsonStrings(expected, actual)

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
            JsonComparator.compareJsonStrings(
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

        val differences = JsonComparator.compareJsonStrings(expected, actual)

        assertEquals(1, differences.size)
        assertEquals("array[1]", differences[0].path)
        assertEquals("\"B\"", differences[0].expected.toString())
        assertEquals("\"C\"", differences[0].actual.toString())
    }

    @Test
    fun `should detect differences in array size`() {
        val expected = """{"array": ["A", "B"]}"""
        val actual = """{"array": ["A"]}"""

        val differences = JsonComparator.compareJsonStrings(expected, actual)

        assertEquals(1, differences.size)
        assertEquals("array[1]", differences[0].path)
        assertEquals("\"B\"", differences[0].expected.toString())
        assertEquals("null", differences[0].actual.toString())
    }

    @Test
    fun `should detect differences when one field is missing`() {
        val expected = """{"A": "B"}"""
        val actual = """{}"""

        val differences = JsonComparator.compareJsonStrings(expected, actual)

        assertEquals(1, differences.size)
        assertEquals("A", differences[0].path)
        assertEquals("\"B\"", differences[0].expected.toString())
        assertEquals("null", differences[0].actual.toString())
    }

    @Test
    fun `should detect differences when extra field is present`() {
        val expected = """{}"""
        val actual = """{"field1": "value1", "field2": 1}"""

        val differences = JsonComparator.compareJsonStrings(expected, actual)

        assertEquals(2, differences.size)
        assertEquals("field1", differences[0].path)
        assertEquals("null", differences[0].expected.toString())
        assertEquals("\"value1\"", differences[0].actual.toString())
    }

    @ParameterizedTest
    @CsvSource(
        delimiter = ';',
        value = [
            """{"a": 1.0};{"a": 1}""",
            """{"a": 3.7534466E7};{"a": 37534466}""",
        ],
    )
    fun `should not detect differences due to number formatting`(
        expected: String,
        actual: String,
    ) {
        val differences = JsonComparator.compareJsonStrings(expected, actual)
        assertEquals(0, differences.size)
    }

    @ParameterizedTest
    @CsvSource(
        delimiter = ';',
        value = [
            """{"a": {"b": null, "c": 1}}""",
            """{"a": {"b": null, "c": {"d": [], "e": "Test"}}}""",
            """{"a": {}, "b": [], "c": {"d": null, "e": "Test"}}}""",
        ],
    )
    fun `nested objects with partially null values should not be equal to null`(jsonString: String) {
        assertEquals(JsonComparator.isFullyNullObject(testObjectMapper.readTree(jsonString)), false)
    }
}
