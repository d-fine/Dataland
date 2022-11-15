package org.dataland.datalandbackend.annotations

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class DataTypesExtractorTest {

    @Test
    fun `check if extracting data types works as expected`() {
        val expectedTypes = listOf("eutaxonomy-financials", "eutaxonomy-non-financials", "sfdr", "lksg")
        val dataTypes = DataTypesExtractor().getAllDataTypes()
        Assertions.assertTrue(
            dataTypes.containsAll(expectedTypes),
            "Found $dataTypes instead of the expected $expectedTypes"
        )
    }
}
