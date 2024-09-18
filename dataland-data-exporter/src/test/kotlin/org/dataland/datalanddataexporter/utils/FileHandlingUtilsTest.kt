package org.dataland.datalanddataexporter.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FileHandlingUtilsTest {
    private val testTransformationConfig = "./csv/configs/transformation.config"

    private val expectedTransformationRules = mapOf(
        "presentMapping" to "presentHeader",
        "notMapped" to "",
        "mappedButNoData" to "mappedButNoDataHeader",
        "nested.nestedMapping" to "nestedHeader",
    )

    @Test
    fun `check that readTransformationConfig returns correct transformation rules`() {
        val transformationRules = FileHandlingUtils.readTransformationConfig(testTransformationConfig)
        assertEquals(expectedTransformationRules, transformationRules)
    }
}
