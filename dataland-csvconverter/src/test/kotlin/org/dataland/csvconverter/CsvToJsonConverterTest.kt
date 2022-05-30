package org.dataland.csvconverter

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.lang.IllegalArgumentException

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

    @Test
    fun `reporting obligation with incorrect input value throws exception`() {
        checkThatProcessingFileThrowsErrorWithMessage(
            "./build/resources/csvTestDataThrowingNFRDRequiredError.csv",
            "Could not determine reportObligation:"
        )
    }

    @Test
    fun `assurance with incorrect input value throws exception`() {
        val expectedErrorMessage = "Could not determine attestation:"
        val filePath = "./build/resources/csvTestDataThrowingAssuranceError.csv"
        checkThatProcessingFileThrowsErrorWithMessage(filePath, expectedErrorMessage)
    }

    private fun checkThatProcessingFileThrowsErrorWithMessage(
        filePath: String,
        expectedErrorMessage: String
    ) {
        val converter = CsvToJsonConverter()
        converter.parseCsvFile(File(filePath).path)
        val exceptionThatWasThrown = assertThrows<IllegalArgumentException>(
            message = "Checking that invalid data results in failure parsing reporting obligation",
            executable = {
                converter.buildListOfCompanyInformationWithEuTaxonomyData()
            }
        )
        val found = exceptionThatWasThrown.message!!
        assertTrue(
            found.contains(expectedErrorMessage),
            "checking that the correct exception message string is present. " +
                "Found: $found, expected: $expectedErrorMessage"
        )
    }
}
