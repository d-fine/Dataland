package org.dataland.datalandbackend.annotations

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class DataTypesExtractorTest {

    @Test
    fun `check if extracting data types works as expected`() {
        val expectedTypes = listOf("eutaxonomy-financials", "eutaxonomy-non-financials", "lksg", "sfdr", "sme")
        val dataTypes = DataTypesExtractor().getAllDataTypes()
        Assertions.assertEquals(
            expectedTypes.toSet(),
            dataTypes.toSet(),
            "Found $dataTypes instead of the expected $expectedTypes"
        )
    }
}
