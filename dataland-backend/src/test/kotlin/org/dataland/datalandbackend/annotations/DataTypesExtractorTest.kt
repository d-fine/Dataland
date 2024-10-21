package org.dataland.datalandbackend.annotations

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class DataTypesExtractorTest {
    @Test
    fun `check if extracting data types works as expected`() {
        val expectedTypes =
            setOf(
                "eutaxonomy-financials",
                "eutaxonomy-non-financials",
                "lksg",
                "sfdr",
                "vsme",
                "p2p",
                "esg-questionnaire",
                "heimathafen",
                "additional-company-information",
                "nuclear-and-gas",
            )
        val dataTypes = DataTypesExtractor().getAllDataTypes()
        Assertions.assertTrue(
            dataTypes.toSet().containsAll(expectedTypes),
            "Found $dataTypes instead of the expected $expectedTypes",
        )
        Assertions.assertTrue(
            expectedTypes.containsAll(dataTypes.toSet()),
            "Found $dataTypes instead of the expected $expectedTypes",
        )
    }
}
