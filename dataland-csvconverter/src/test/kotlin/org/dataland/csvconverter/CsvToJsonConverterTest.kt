package org.dataland.csvconverter

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class CsvToJsonConverterTest {
    private val objectMapper = ObjectMapper().findAndRegisterModules()
    private val testDataProvider = TestDataProvider(objectMapper)
    private val csvParser = CsvToJsonConverter(File("./src/test/resources/csvTestData.csv").path)
        .setEuroUnitConverter("1")

    @Test
    fun `read csv and check that the company information objects are as expected`() {
        val parsedCompanies = csvParser.buildListOfCompanyInformation()
        val readCompanies = testDataProvider.getAllCompanies()
        assertTrue(
            parsedCompanies.size == readCompanies.size,
            "Size mismatch: the parsed list contains ${parsedCompanies.size} and the read list " +
                "contains ${readCompanies.size} elements."
        )
        for (index in 0..parsedCompanies.size) {
            println(index)
            println(readCompanies[index])
            println(parsedCompanies[index])
            assertEquals(readCompanies[index], parsedCompanies[index], "The list of read and parsed company information did not match.")
        }
        //assertEquals(readCompanies, parsedCompanies, "The list of read and parsed company information did not match.")
    }

    @Test
    fun `read csv and check that the generated EU Taxonomy objects are as expected`() {
        val parsedEuTaxonomyData = csvParser.buildListOfEuTaxonomyData()
        val readEuTaxonomyData = testDataProvider.getAllData()
        assertTrue(
            parsedEuTaxonomyData.size == readEuTaxonomyData.size,
            "Size mismatch: the parsed list contains" +
                " ${parsedEuTaxonomyData.size} and the read list contains ${readEuTaxonomyData.size} elements."
        )
        assertEquals(
            readEuTaxonomyData, parsedEuTaxonomyData,
            "The list of read and parsed EU Taxonomy data did not match."
        )
    }
}
