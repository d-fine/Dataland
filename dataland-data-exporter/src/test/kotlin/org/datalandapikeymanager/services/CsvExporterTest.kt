package org.datalandapikeymanager.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandapikeymanager.DatalandDataExporter
import org.dataland.datalanddataexporter.services.CsvExporter
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.io.File

@SpringBootTest(classes = [DatalandDataExporter::class])
class CsvExporterTest(
    @Autowired val testCsvExporter: CsvExporter,
) {
    val testTransformationConfig = "./csv/configs/transformation.config"
    val inputJson = "./src/test/resources/csv/input.json"
    val expectedTransformationRules = mapOf("companyName" to "Name", "companyId" to "ID", "lei" to "")
    val expectedHeaders = listOf("Name", "ID")
    val expectedCsvData = mapOf("Name" to "Corp Inc.", "ID" to "123")

    @Test
    fun `check that a duplicated header entry in the transformation rules throws an error`() {
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            testCsvExporter.getHeaders(mapOf("key1" to "header1", "key2" to "header1"))
        }
    }

    @Test
    fun `check that getHeaders returns correct headers`() {
        val headers = testCsvExporter.getHeaders(expectedTransformationRules)
        Assertions.assertEquals(expectedHeaders, headers)
    }

    @Test
    fun `check that readTransformationConfig returns correct transformation rules`() {
        val transformationRules = testCsvExporter.readTransformationConfig(testTransformationConfig)
        Assertions.assertEquals(expectedTransformationRules, transformationRules)
    }

    @Test
    fun `check that mapJsonToCsv returns correct csv data`() {
        val jsonNode = ObjectMapper().readTree(File(inputJson))
        val csvData = testCsvExporter.mapJsonToCsv(jsonNode, expectedTransformationRules)
        Assertions.assertEquals(expectedCsvData, csvData)
    }
}
