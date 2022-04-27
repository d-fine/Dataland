package org.dataland.csvconverter

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class CsvToJsonConverterTest {
    private val objectMapper = ObjectMapper().findAndRegisterModules()
    private val testDataProvider = TestDataProvider(objectMapper)

    @Test
    fun `Read csv and check that the generated objects are as expected`() {
        val csvParser = CsvToJsonConverter(File("./src/test/resources/csvTestData.csv").path)
        csvParser.euroUnitConverter = "1"
        val parsedCompanies = csvParser.buildListOfCompanyInformation()
        val parsedEuTaxonomyData = csvParser.buildListOfEuTaxonomyData()
        val readCompanies = testDataProvider.getAllCompanies()
        val readEuTaxonomyData = testDataProvider.getAllData()
        assertTrue(
            parsedCompanies.size == readCompanies.size,
            "Size mismatch: the parsed list contains ${parsedCompanies.size} and the read list " +
                "contains ${readCompanies.size} elements."
        )
        assertEquals(readCompanies, parsedCompanies, "The list of read and parsed company information did not match.")
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
