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

    @Test
    fun `check that getTimestamp returns correct timestamp format`() {
        val timestamp = FileHandlingUtils.getTimestamp()
        assertEquals(13, timestamp.length)
        val regex = "^202[4-9][0-1][0-9][0-3][0-9]_[0-2][0-9][0-5][0-9]$".toRegex()
        assert(regex.matches(timestamp))
    }
}
