package org.dataland.e2etests.utils

import org.dataland.datalandbackend.openApiClient.model.SfdrData
import org.junit.jupiter.api.Assertions.assertEquals

class DataPointTestUtils {
    /**
     * Asserts that the data of two SfdrData objects are equal, ignoring the publication dates
     * @param expected The expected SfdrData object
     * @param actual The actual SfdrData object
     */
    fun assertDataEqualsIgnoringPublicationDates(
        expected: SfdrData,
        actual: SfdrData,
    ) {
        val expectedString = expected.toString()
        val allReferencedReportPublicationDates =
            expected.general.general.referencedReports
                ?.map { it.value.publicationDate.toString() } ?: emptyList()
        val actualStringWithAllReferencedReportDatesReplaced = replaceAllByNull(actual.toString(), allReferencedReportPublicationDates)
        val expectedStringWithAllReferencedReportDatesReplaced = replaceAllByNull(expectedString, allReferencedReportPublicationDates)
        assertEquals(expectedStringWithAllReferencedReportDatesReplaced, actualStringWithAllReferencedReportDatesReplaced)
    }

    private fun replaceAllByNull(
        input: String,
        toReplace: List<String>,
    ): String {
        var result = input
        for (replacement in toReplace) {
            result = result.replace(replacement, "null")
        }
        return result
    }
}
