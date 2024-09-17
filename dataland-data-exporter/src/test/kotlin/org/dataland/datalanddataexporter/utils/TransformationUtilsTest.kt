package org.dataland.datalanddataexporter.utils

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File
import org.dataland.datalanddataexporter.utils.TransformationUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows


class TransformationUtilsTest {
    val testTransformationConfig = "./csv/configs/transformation.config"
    //val inputJson = this.javaClass.classLoader.getResourceAsStream("./src/test/resources/csv/input.json")
    val inputJson = File("./src/test/resources/csv/input.json")
    //val inconsistentJson = this.javaClass.classLoader.getResourceAsStream("./src/test/resources/csv/inconsistent.json")
    val inconsistentJson = File("./src/test/resources/csv/inconsistent.json")
    val expectedTransformationRules = mapOf(
        "presentMapping" to "presentHeader",
        "notMapped" to "",
        "mappedButNoData" to "mappedButNoDataHeader",
        "nested.nestedMapping" to "nestedHeader",
    )
    val expectedHeaders = listOf("presentHeader", "mappedButNoDataHeader", "nestedHeader")
    val expectedCsvData =
        mapOf("presentHeader" to "Here", "mappedButNoDataHeader" to "", "nestedHeader" to "NestedHere")
    val expectedCsvFileContent =
        "\"nestedHeader\"|\"presentHeader\"|\"mappedButNoDataHeader\"\n\"NestedHere\"|\"Here\"|\n"


    @Test
    fun `check that a duplicated header entry in the transformation rules throws an error`() {
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            TransformationUtils.getHeaders(mapOf("key1" to "header1", "key2" to "header1"))
        }
    }

    @Test
    fun `check that getHeaders returns correct headers`() {
        val headers = TransformationUtils.getHeaders(expectedTransformationRules)
        Assertions.assertEquals(expectedHeaders, headers)
    }

    @Test
    fun `check that checkConsistency does not throw an exception for consistent data`() {
        val jsonNode = ObjectMapper().readTree(inputJson)
        assertDoesNotThrow { TransformationUtils.checkConsistency(jsonNode, expectedTransformationRules) }
    }

    @Test
    fun `check that checkConsistency throws an exception for inconsistent data`() {
        val jsonNode = ObjectMapper().readTree(inconsistentJson)
        assertThrows<IllegalArgumentException> { TransformationUtils.checkConsistency(jsonNode, expectedTransformationRules) }
    }

    @Test
    fun `check that readTransformationConfig returns correct transformation rules`() {
        val transformationRules = TransformationUtils.readTransformationConfig(testTransformationConfig)
        Assertions.assertEquals(expectedTransformationRules, transformationRules)
    }

}