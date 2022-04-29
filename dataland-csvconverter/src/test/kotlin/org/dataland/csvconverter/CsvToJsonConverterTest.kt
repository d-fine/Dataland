package org.dataland.csvconverter

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class CsvToJsonConverterTest {
    private val objectMapper = ObjectMapper().findAndRegisterModules()
    private val testDataProvider = TestDataProvider(objectMapper)

    private fun getConverter(): CsvToJsonConverter {
        val converter = CsvToJsonConverter()
        converter.parseCsvFile(File("./build/resources/csvTestData.csv").path)
        return converter
    }

    @Test
    fun `read csv and check that the company information objects are as expected`() {
        val actualCompanies = getConverter().buildListOfCompanyInformation()
        val expectedCompanies = testDataProvider.getAllCompanies()
        assertTrue(
            actualCompanies.size == expectedCompanies.size,
            "Size mismatch: the parsed list contains ${actualCompanies.size} and the read list " +
                "contains ${expectedCompanies.size} elements."
        )
        assertEquals(
            expectedCompanies, actualCompanies,
            "The list of read and parsed company information did not match."
        )
    }

    @Test
    fun `read csv and check that the generated EU Taxonomy objects are as expected`() {
        val actualEuTaxonomyData = getConverter().buildListOfEuTaxonomyData()
        val expectedEuTaxonomyData = testDataProvider.getAllData()
        assertTrue(
            actualEuTaxonomyData.size == expectedEuTaxonomyData.size,
            "Size mismatch: the parsed list contains" +
                " ${actualEuTaxonomyData.size} and the read list contains ${expectedEuTaxonomyData.size} elements."
        )
        assertEquals(
            expectedEuTaxonomyData, actualEuTaxonomyData,
            "The list of read and parsed EU Taxonomy data did not match."
        )
    }
}
