package org.dataland.datalandbackend.annotations

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class DataTypesExtractorTest {

    @Test
    fun `check if extracting data types works as expected`() {
        val expectedTypes = listOf("EuTaxonomyNonFinancialData", "EuTaxonomyFinancialData")
        val dataTypes = DataTypesExtractor().getAllDataTypes()
        Assertions.assertTrue(
            dataTypes.sorted() == expectedTypes.sorted(),
            "Found $dataTypes instead of the expected $expectedTypes"
        )
    }
}
