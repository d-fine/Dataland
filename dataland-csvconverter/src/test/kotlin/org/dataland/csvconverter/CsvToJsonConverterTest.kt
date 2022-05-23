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
    fun `read csv and check that the company information and EU Taxonomy objects are as expected`() {
        val actualCompanyInformationWithEuTaxonomyData =
            getConverter().buildListOfCompanyInformationWithEuTaxonomyData()
        val expectedCompanyInformationWithEuTaxonomyData = testDataProvider.getAllCompanyInformationWithEuTaxonomyData()
        assertTrue(
            actualCompanyInformationWithEuTaxonomyData.size == expectedCompanyInformationWithEuTaxonomyData.size,
            "Size mismatch: the parsed list contains ${actualCompanyInformationWithEuTaxonomyData.size} " +
                "and the read list contains ${expectedCompanyInformationWithEuTaxonomyData.size} elements."
        )
        assertEquals(
            expectedCompanyInformationWithEuTaxonomyData, actualCompanyInformationWithEuTaxonomyData,
            "The list of read and parsed company information did not match."
        )
    }
}
