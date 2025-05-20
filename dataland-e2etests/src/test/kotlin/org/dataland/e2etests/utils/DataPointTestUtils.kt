package org.dataland.e2etests.utils

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.dataland.datalandbackend.openApiClient.infrastructure.BigDecimalAdapter
import org.dataland.datalandbackend.openApiClient.infrastructure.BigIntegerAdapter
import org.dataland.datalandbackend.openApiClient.model.CompanyReport
import org.dataland.datalandbackend.openApiClient.model.SfdrData
import org.junit.jupiter.api.Assertions.assertEquals
import java.text.SimpleDateFormat
import java.time.LocalDate

class IgnoreLocalDateAdapter : JsonAdapter<LocalDate>() {
    override fun fromJson(reader: JsonReader): LocalDate? =
        throw UnsupportedOperationException("This adapter should only be used for serialization")

    override fun toJson(
        writer: JsonWriter,
        value: LocalDate?,
    ) {
        writer.nullValue()
    }
}

/**
 * Asserts that the data of two objects are equal, ignoring date fields
 * @param expected The expected object
 * @param actual The actual object
 */
inline fun <reified T> assertDataEqualsIgnoringDates(
    expected: T,
    actual: T,
    referencedReportsGetter: (T) -> Map<String, CompanyReport>?,
) {
    val moshi =
        Moshi
            .Builder()
            .add(BigDecimalAdapter())
            .add(BigIntegerAdapter())
            .add(LocalDate::class.java, IgnoreLocalDateAdapter())
            .addLast(KotlinJsonAdapterFactory())
    val jsonAdapter =
        moshi
            .build()
            .adapter(T::class.java)
    val referencedReportsAdapter =
        moshi
            .build()
            .adapter(Map::class.java)

    val referencedReportsFromActual = referencedReportsAdapter.toJson(referencedReportsGetter(actual))
    val referencedReportsFromExpected = referencedReportsAdapter.toJson(referencedReportsGetter(expected))

    var actualJson = jsonAdapter.toJson(actual)
    if (referencedReportsFromActual != null) {
        actualJson = actualJson.replace(referencedReportsFromActual, "null")
    }

    var expectedJson = jsonAdapter.toJson(expected)
    if (referencedReportsFromExpected != null) {
        expectedJson = expectedJson.replace(referencedReportsFromExpected, "null")
    }

    DataPointTestUtils.assertReferencedReportsEquals(referencedReportsGetter(expected), referencedReportsGetter(actual))
    assertEquals(expectedJson, actualJson)
}

object DataPointTestUtils {
    fun assertReferencedReportsEquals(
        expected: Map<String, CompanyReport>?,
        actual: Map<String, CompanyReport>?,
    ) {
        if (expected == null || actual == null) {
            return assertEquals(actual, expected)
        }

        assertEquals(expected.keys, actual.keys)
        expected.keys.forEach {
            assertCompanyReportEquals(expected[it]!!, actual[it]!!)
        }
    }

    private fun assertCompanyReportEquals(
        expected: CompanyReport,
        actual: CompanyReport,
    ) {
        assertEquals(expected.copy(publicationDate = null), actual.copy(publicationDate = null))
    }

    /**
     * Asserts that the data of two SfdrData objects are equal, comparing the referenced reports directly
     * but ignoring the publication dates in all other fields
     * @param expected The expected SfdrData object
     * @param actual The actual SfdrData object
     */
    fun assertSfdrDataEquals(
        expected: SfdrData,
        actual: SfdrData,
    ) {
        assertDataEqualsIgnoringDates(
            expected,
            actual,
            { it.general?.general?.referencedReports },
        )
    }

    /**
     * Removes all entries from the QA report that are not attempted and null-ish
     * @param qaReport The QA report to remove the entries from
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
