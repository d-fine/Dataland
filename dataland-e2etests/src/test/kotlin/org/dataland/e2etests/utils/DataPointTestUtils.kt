package org.dataland.e2etests.utils

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandbackend.openApiClient.model.SfdrData
import org.junit.jupiter.api.Assertions.assertEquals
import java.text.SimpleDateFormat

class DataPointTestUtils {
    /**
     * Asserts that the data of two SfdrData objects are equal, ignoring the publication dates
     * @param expected The expected SfdrData object
     * @param actual The actual SfdrData object
     */
    fun assertDataEqualsIgnoringPublicationDates(
        expected: SfdrData,
        actual: SfdrData,
        publicationDates: List<String>? = getAllPublicationDates(expected),
    ) {
        val actualStringWithAllReferencedReportDatesReplaced = publicationDates?.let { replaceAllByNull(actual.toString(), it) }
        val expectedStringWithAllReferencedReportDatesReplaced = publicationDates?.let { replaceAllByNull(expected.toString(), it) }
        assertEquals(expectedStringWithAllReferencedReportDatesReplaced, actualStringWithAllReferencedReportDatesReplaced)
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
        assertEquals(expected.general.general.referencedReports, actual.general.general.referencedReports)
        assertDataEqualsIgnoringPublicationDates(
            getCopyWithoutReferencedReports(expected),
            getCopyWithoutReferencedReports(actual),
            getAllPublicationDates(expected),
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
                "{\"comment\":\"\",\"verdict\":\"QaNotAttempted\",\"correctedData\":{\"value\":null,\"quality\":null,\"comment\":null,\"dataSource\":null}}",
                "{\"comment\":\"\",\"verdict\":\"QaNotAttempted\",\"correctedData\":{\"value\":null,\"quality\":null,\"comment\":null,\"dataSource\":null,\"currency\":null}}",
            )
        val objectMapper = jacksonObjectMapper().findAndRegisterModules().setDateFormat(SimpleDateFormat("yyyy-MM-dd"))
        val updatedInput = replaceAllByNull(objectMapper.writeValueAsString(qaReport), replacement)
        return objectMapper.readValue(updatedInput, org.dataland.datalandqaservice.openApiClient.model.SfdrData::class.java)
    }

    private fun getAllPublicationDates(data: SfdrData): List<String> =
        data.general.general.referencedReports
            ?.map { it.value.publicationDate.toString() } ?: emptyList()

    private fun getCopyWithoutReferencedReports(data: SfdrData): SfdrData =
        data.copy(
            general =
                data.general.copy(
                    general =
                        data.general.general.copy(
                            referencedReports = null,
                        ),
                ),
        )

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
