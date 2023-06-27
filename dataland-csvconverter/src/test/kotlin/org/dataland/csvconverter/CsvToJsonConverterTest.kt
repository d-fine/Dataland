package org.dataland.csvconverter

import org.dataland.csvconverter.json.JsonConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File
/*
class CsvToJsonConverterTest {
    private val testDataProvider = TestDataProvider(JsonConfig.objectMapper)

    private fun getConverter(filePath: String): CsvToJsonConverter {
        val converter = CsvToJsonConverter()
        converter.parseCsvFile(File(filePath).path)
        return converter
    }

    @Test
    fun `read csv and check that the company information and EU Taxonomy objects are as expected for non financials`() {
        val actualCompanyInformationWithEuTaxonomyDataForNonFinancials =
            getConverter("./build/resources/test/csvTestEuTaxonomyDataForNonFinancials.csv")
                .parseEuTaxonomyNonFinancialData()
        val expectedCompanyInformationWithEuTaxonomyDataForNonFinancials = testDataProvider
            .getAllCompanyInformationWithEuTaxonomyDataForNonFinancials()
        assertTrue(
            actualCompanyInformationWithEuTaxonomyDataForNonFinancials.size ==
                expectedCompanyInformationWithEuTaxonomyDataForNonFinancials.size,
            "Size mismatch: the parsed list contains " +
                "${actualCompanyInformationWithEuTaxonomyDataForNonFinancials.size} " +
                "and the read list contains " +
                "${expectedCompanyInformationWithEuTaxonomyDataForNonFinancials.size} elements.",
        )
        expectedCompanyInformationWithEuTaxonomyDataForNonFinancials.forEachIndexed { index, companyAssociatedData ->
            assertEquals(
                companyAssociatedData.companyInformation,
                actualCompanyInformationWithEuTaxonomyDataForNonFinancials[index].companyInformation,
                "The company information at index $index did not match.",
            )
            // referenced reports are excluded (expected deviation due to the ISIN being added for real data)
            assertEquals(
                companyAssociatedData.t.copy(referencedReports = emptyMap()),
                actualCompanyInformationWithEuTaxonomyDataForNonFinancials[index].t
                    .copy(referencedReports = emptyMap()),
                "The EU Taxonomy data at index $index did not match.",
            )
        }
    }

    @Test
    fun `read csv and check that the company information and EU Taxonomy objects are as expected for financials`() {
        val actualCompanyInformationWithEuTaxonomyDataForFinancials =
            getConverter("./build/resources/test/csvTestEuTaxonomyDataForFinancials.csv")
                .parseEuTaxonomyFinancialData()
        val expectedCompanyInformationWithEuTaxonomyDataForFinancials = testDataProvider
            .getAllCompanyInformationWithEuTaxonomyDataForFinancials()
        assertTrue(
            actualCompanyInformationWithEuTaxonomyDataForFinancials.size ==
                expectedCompanyInformationWithEuTaxonomyDataForFinancials.size,
            "Size mismatch: the parsed list contains " +
                "${actualCompanyInformationWithEuTaxonomyDataForFinancials.size} " +
                "and the read list contains " +
                "${expectedCompanyInformationWithEuTaxonomyDataForFinancials.size} elements.",
        )
        actualCompanyInformationWithEuTaxonomyDataForFinancials.forEachIndexed { index, companyAssociatedData ->
            assertEquals(
                companyAssociatedData.companyInformation,
                expectedCompanyInformationWithEuTaxonomyDataForFinancials[index].companyInformation,
                "The company information at index $index did not match.",
            )
            // referenced reports are excluded (expected deviation due to the ISIN being added for real data)
            assertEquals(
                companyAssociatedData.t.copy(referencedReports = emptyMap()),
                expectedCompanyInformationWithEuTaxonomyDataForFinancials[index].t.copy(referencedReports = emptyMap()),
                "The EU Taxonomy data at index $index did not match.",
            )
        }
    }
}
 */
