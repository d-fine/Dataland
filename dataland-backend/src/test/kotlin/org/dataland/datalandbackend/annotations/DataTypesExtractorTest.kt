package org.dataland.datalandbackend.annotations

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class DataTypesExtractorTest {

    @Test
    fun `check if the allowed data types is not empty`() {
        val dataTypes = DataTypesExtractor().getAllDataTypes()
        Assertions.assertFalse(dataTypes.isEmpty())
        Assertions.assertTrue(
            dataTypes.contains("EuTaxonomyData"),
            "EuTaxonomyData should be contained in annotation processed data types"
        )
        Assertions.assertFalse(
            dataTypes.contains("StorableDataSet"),
            "StorableDataSet should not be contained in annotation processed data types"
        )
    }
}
