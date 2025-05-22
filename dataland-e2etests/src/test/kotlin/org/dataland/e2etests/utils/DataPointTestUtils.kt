package org.dataland.e2etests.utils

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandbackend.openApiClient.model.SfdrData
import org.dataland.datalandbackendutils.utils.JsonComparator
import org.junit.jupiter.api.Assertions.assertEquals
import java.text.SimpleDateFormat

/**
 * Function to assert that two objects are equal, ignoring the given keys.
 * Comparison is done on the JSON representation of the objects using the JsonComparator
 */
inline fun <reified T> assertEqualsByJsonComparator(
    expected: T,
    actual: T,
    jsonComparisonOptions: JsonComparator.JsonComparisonOptions = JsonComparator.JsonComparisonOptions(),
) {
    val differences =
        JsonComparator.compareClasses(
            expected,
            actual,
            jsonComparisonOptions,
        )
    assertEquals(0, differences.size, "Assertion failed. There are differences: $differences")
}

object DataPointTestUtils {
    /**
     * Removes all entries from the QA report that are not attempted and null-ish
     * @param qaReport The QA report from which the entries are to be removed
     * @return The QA report with the entries removed
     */
    fun removeEmptyEntries(
        qaReport: org.dataland.datalandqaservice.openApiClient.model.SfdrData,
    ): org.dataland.datalandqaservice.openApiClient.model.SfdrData {
        val replacement =
            listOf(
                "{\"comment\":\"\",\"verdict\":\"QaNotAttempted\",\"correctedData\":{\"value\":null," +
                    "\"quality\":null,\"comment\":null,\"dataSource\":null}}",
                "{\"comment\":\"\",\"verdict\":\"QaNotAttempted\",\"correctedData\":{\"value\":null," +
                    "\"quality\":null,\"comment\":null,\"dataSource\":null,\"currency\":null}}",
            )
        val objectMapper = jacksonObjectMapper().findAndRegisterModules().setDateFormat(SimpleDateFormat("yyyy-MM-dd"))
        val updatedInput = replaceAllByNull(objectMapper.writeValueAsString(qaReport), replacement)
        return objectMapper.readValue(updatedInput, org.dataland.datalandqaservice.openApiClient.model.SfdrData::class.java)
    }

    private fun replaceAllByNull(
        input: String,
        replacements: List<String>,
    ): String {
        var result = input
        replacements.forEach {
            result = result.replace(it, "null")
        }
        return result
    }
}
