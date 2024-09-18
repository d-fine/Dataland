package org.dataland.datalanddataexporter.utils

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.io.File

class TransformationUtilsTest {
    val testTransformationConfig = "./csv/configs/transformation.config"

    // val inputJson = this.javaClass.classLoader.getResourceAsStream("./src/test/resources/csv/input.json")
    val inputJson = File("./src/test/resources/csv/input.json")

    // val inconsistentJson =
    // this.javaClass.classLoader.getResourceAsStream("./src/test/resources/csv/inconsistent.json")
    val inconsistentJson = File("./src/test/resources/csv/inconsistent.json")
    val expectedTransformationRules = mapOf(
        "presentMapping" to "presentHeader",
        "notMapped" to "",
        "mappedButNoData" to "mappedButNoDataHeader",
        "nested.nestedMapping" to "nestedHeader",
    )
    val expectedHeaders = listOf("presentHeader", "mappedButNoDataHeader", "nestedHeader")
    val expectedJsonPaths = listOf("presentMapping", "notMapped", "nested.nestedMapping")

    @Test
    fun `check that the retrieved JSON paths are as exüected`() {
        val jsonNode = ObjectMapper().readTree(inputJson)
        val result = TransformationUtils.getNonArrayLeafNodeFieldNames(jsonNode, "")
        assertEquals(expectedJsonPaths, result)
    }

    @Test
    fun `check that a duplicated header entry in the transformation rules throws an error`() {
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            TransformationUtils.getHeaders(mapOf("key1" to "header1", "key2" to "header1"))
        }
    }

    @Test
    fun `check that getHeaders returns correct headers`() {
        val headers = TransformationUtils.getHeaders(expectedTransformationRules)
        assertEquals(expectedHeaders, headers)
    }

    @Test
    fun `check that checkConsistency does not throw an exception for consistent data`() {
        val jsonNode = ObjectMapper().readTree(inputJson)
        assertDoesNotThrow { TransformationUtils.checkConsistency(jsonNode, expectedTransformationRules) }
    }

    @Test
    fun `check that checkConsistency throws an exception for inconsistent data`() {
        val jsonNode = ObjectMapper().readTree(inconsistentJson)
        assertThrows<IllegalArgumentException> {
            TransformationUtils.checkConsistency(jsonNode, expectedTransformationRules)
        }
    }

    @Test
    fun `check that readTransformationConfig returns correct transformation rules`() {
        val transformationRules = TransformationUtils.readTransformationConfig(testTransformationConfig)
        assertEquals(expectedTransformationRules, transformationRules)
    }
}
