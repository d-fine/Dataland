package org.dataland.datalanddataexporter.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandapikeymanager.DatalandDataExporter
import org.dataland.datalanddataexporter.services.CsvExporter
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.io.File
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

@SpringBootTest(classes = [DatalandDataExporter::class])
class CsvExporterTest(
    @Autowired val testCsvExporter: CsvExporter,
) {
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
    fun `check that mapJsonToCsv returns correct csv data`() {
        val jsonNode = ObjectMapper().readTree(inputJson)
        val csvData = testCsvExporter.mapJsonToCsv(jsonNode, expectedTransformationRules)
        Assertions.assertEquals(expectedCsvData, csvData)
    }

    /*@Test
    fun `check that the csv-file writen for the conversion is as expected`() {
        // TODO rethink this test
        val csvFile = File("./src/test/resources/csv/output/output.csv")
        val jsonNode = ObjectMapper().readTree(File(inputJson))
        val transformationRules = testCsvExporter.readTransformationConfig(testTransformationConfig)
        val csvData = testCsvExporter.mapJsonToCsv(jsonNode, transformationRules)
        val headers = testCsvExporter.getHeaders(transformationRules)
        testCsvExporter.writeCsv(listOf(csvData), csvFile, headers)
        Assertions.assertEquals(expectedCsvFileContent, csvFile.readText())
    }*/


}
