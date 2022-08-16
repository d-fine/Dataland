package org.dataland.csvconverter

import org.dataland.csvconverter.csv.CsvUtils
import org.dataland.csvconverter.json.JsonConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.lang.IllegalArgumentException

class CsvToJsonConverterTest {
    private val testDataProvider = TestDataProvider(JsonConfig.objectMapper)

    private fun getConverter(filePath: String): CsvToJsonConverter {
        CsvUtils.EURO_UNIT_CONVERSION_FACTOR = "1"
        val converter = CsvToJsonConverter()
        converter.parseCsvFile(File(filePath).path)
        return converter
    }

    @Test
    fun `read csv and check that the company information and EU Taxonomy objects are as expected for non-financials`() {
        val actualCompanyInformationWithEuTaxonomyDataForNonFinancials =
            getConverter("./build/resources/csvTestEuTaxonomyDataForNonFinancials.csv")
                .parseEuTaxonomyNonFinancialData()
        val expectedCompanyInformationWithEuTaxonomyDataForNonFinancials = testDataProvider
            .getAllCompanyInformationWithEuTaxonomyDataForNonFinancials()
        assertTrue(
            actualCompanyInformationWithEuTaxonomyDataForNonFinancials.size ==
                expectedCompanyInformationWithEuTaxonomyDataForNonFinancials.size,
            "Size mismatch: the parsed list contains " +
                "${actualCompanyInformationWithEuTaxonomyDataForNonFinancials.size} " +
                "and the read list contains " +
                "${expectedCompanyInformationWithEuTaxonomyDataForNonFinancials.size} elements."
        )
        assertEquals(
            expectedCompanyInformationWithEuTaxonomyDataForNonFinancials,
            actualCompanyInformationWithEuTaxonomyDataForNonFinancials,
            "The list of read and parsed company information did not match."
        )
    }

    @Test
    fun `read csv and check that the company information and EU Taxonomy objects are as expected for financials`() {
        val actualCompanyInformationWithEuTaxonomyDataForFinancials =
            getConverter("./build/resources/csvTestEuTaxonomyDataForFinancials.csv")
                .parseEuTaxonomyFinancialData()
        val expectedCompanyInformationWithEuTaxonomyDataForFinancials = testDataProvider
            .getAllCompanyInformationWithEuTaxonomyDataForFinancials()
        assertTrue(
            actualCompanyInformationWithEuTaxonomyDataForFinancials.size ==
                expectedCompanyInformationWithEuTaxonomyDataForFinancials.size,
            "Size mismatch: the parsed list contains " +
                "${actualCompanyInformationWithEuTaxonomyDataForFinancials.size} " +
                "and the read list contains " +
                "${expectedCompanyInformationWithEuTaxonomyDataForFinancials.size} elements."
        )
        assertEquals(
            expectedCompanyInformationWithEuTaxonomyDataForFinancials,
            actualCompanyInformationWithEuTaxonomyDataForFinancials,
            "The list of read and parsed company information did not match."
        )
    }

    @Test
    fun `reporting obligation with incorrect input value throws exception`() {
        checkThatProcessingFileThrowsErrorWithMessage(
            "./build/resources/csvTestEuTaxonomyDataForNonFinancialsThrowingNFRDRequiredError.csv",
            "Could not determine reportObligation:"
        )
    }

    @Test
    fun `missing market capitalisation throws exception`() {
        checkThatProcessingFileThrowsErrorWithMessage(
            "./build/resources/csvTestEuTaxonomyDataForNonFinancialsThrowingMarketCapNaError.csv",
            "Could not parse market capitalisation for company"
        )
    }

    @Test
    fun `assurance with incorrect input value throws exception`() {
        val expectedErrorMessage = "Could not determine attestation:"
        val filePath = "./build/resources/csvTestEuTaxonomyDataForNonFinancialsThrowingAssuranceError.csv"
        checkThatProcessingFileThrowsErrorWithMessage(filePath, expectedErrorMessage)
    }

    @Test
    fun `execute corner cases in CsvToJsonConverter`() {
        val converter = getConverter("./build/resources/csvTestEuTaxonomyDataForNonFinancialsCornerCases.csv")
        converter.parseEuTaxonomyNonFinancialData()
    }

    private fun checkThatProcessingFileThrowsErrorWithMessage(
        filePath: String,
        expectedErrorMessage: String
    ) {
        val converter = getConverter(filePath)
        val exceptionThatWasThrown = assertThrows<IllegalArgumentException>(
            message = "Checking that invalid data results in failure parsing reporting obligation",
            executable = {
                converter.parseEuTaxonomyNonFinancialData()
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
