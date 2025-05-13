package org.dataland.e2etests.utils

import org.awaitility.Awaitility
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseExportTest<T> {
    protected val apiAccessor = ApiAccessor()

    // Common properties that all export tests need
    protected lateinit var companyWithNullFieldId: String
    protected lateinit var companyWithNullFieldLei: String
    protected lateinit var companyWithNonNullFieldId: String
    protected lateinit var companyWithNonNullFieldLei: String
    protected val reportingPeriod = "1999"

    // Abstract methods that subclasses must implement
    protected abstract fun getTestDataWithNullField(): T

    protected abstract fun getTestDataWithNonNullField(): T

    protected abstract fun getFullTestData(): T

    protected abstract fun getNullFieldName(): String

    protected abstract fun retrieveData(companyId: String): Any

    protected abstract fun uploadData(
        companyId: String,
        data: T,
        reportingPeriod: String,
    )

    protected abstract fun exportDataAsCsv(
        companyIds: List<String>,
        reportingPeriods: List<String>,
        keepValueFieldsOnly: Boolean = true,
    ): File

    private fun exportDataAsCsvWithMetadata(
        companyIds: List<String>,
        reportingPeriods: List<String>,
    ): File = exportDataAsCsv(companyIds, reportingPeriods, keepValueFieldsOnly = false)

    protected abstract fun exportDataAsExcel(
        companyIds: List<String>,
        reportingPeriods: List<String>,
    ): File

    /**
     * Uploads a company with a randomly generated Legal Entity Identifier (LEI) and associates it with the
     * API accessor.
     *
     * @return A pair where the first element is the ID of the uploaded company and the second element is the generated LEI.
     */
    private fun uploadCompanyWithRandomLei(): Pair<String, String> {
        val lei = UUID.randomUUID().toString()
        return Pair(apiAccessor.uploadOneCompanyWithIdentifiers(lei = lei)!!.actualStoredCompany.companyId, lei)
    }

    /**
     * Sets up test companies and their associated data for use in export tests.
     */
    protected fun setupCompaniesAndData() {
        // Create two test companies with randomly generated LEIs
        val companyWithNullFieldIdAndLei = uploadCompanyWithRandomLei()
        val companyWithNonNullFieldIdAndLei = uploadCompanyWithRandomLei()

        companyWithNullFieldId = companyWithNullFieldIdAndLei.first
        companyWithNullFieldLei = companyWithNullFieldIdAndLei.second
        companyWithNonNullFieldId = companyWithNonNullFieldIdAndLei.first
        companyWithNonNullFieldLei = companyWithNonNullFieldIdAndLei.second

        // Upload test data with null field for first company
        uploadData(
            companyId = companyWithNullFieldId,
            data = getTestDataWithNullField(),
            reportingPeriod = reportingPeriod,
        )

        // Upload test data with non-null field for second company
        uploadData(
            companyId = companyWithNonNullFieldId,
            data = getTestDataWithNonNullField(),
            reportingPeriod = reportingPeriod,
        )

        waitForDataAvailability()
    }

    /**
     * Waits for the data associated with specific test companies to become available.
     *
     * This method uses polling to repeatedly check the availability of data for two test companies
     * identified by their respective IDs (`companyWithNullFieldId` and `companyWithNonNullFieldId`).
     * It ensures that data retrieval for both companies does not throw any exceptions.
     *
     * The method will attempt to verify data availability within a maximum time limit of 10 seconds.
     * Each polling attempt is performed at intervals of 500 milliseconds until the conditions are met.
     *
     * The method is intended to be used as part of the data setup or verification process in export tests,
     * ensuring that the required data is accessible before proceeding with further test operations.
     */
    protected fun waitForDataAvailability() {
        Awaitility
            .await()
            .atMost(10, TimeUnit.SECONDS)
            .pollInterval(500, TimeUnit.MILLISECONDS)
            .untilAsserted {
                assertDoesNotThrow {
                    retrieveData(companyWithNullFieldId)
                    retrieveData(companyWithNonNullFieldId)
                }
            }
    }

    // Common test implementations that can be called from subclasses

    /**
     * Verifies that the CSV export excludes a column corresponding to a null field in the test data.
     * The test ensures that null fields in the source data are properly omitted from the exported CSV file.
     */
    protected fun testCsvExportOmitsColumnForNullField() {
        // Export only the company with null field
        val singleCompanyCsvExport =
            exportDataAsCsv(
                companyIds = listOf(companyWithNullFieldId),
                reportingPeriods = listOf(reportingPeriod),
            )

        ExportTestUtils.validateExportFile(singleCompanyCsvExport, "Single company CSV export")

        val headers = ExportTestUtils.readCsvHeaders(singleCompanyCsvExport)

        // Check that the null field column does NOT exist
        ExportTestUtils.assertColumnPatternExists(
            headers,
            getNullFieldName(),
            shouldExist = false,
            "Single company CSV export with null field company",
        )
    }

    protected fun testCsvExportIncludesColumnForNonNullField() {
        // Export only the company with non-null field
        val singleCompanyCsvExport =
            exportDataAsCsv(
                companyIds = listOf(companyWithNonNullFieldId),
                reportingPeriods = listOf(reportingPeriod),
            )

        ExportTestUtils.validateExportFile(singleCompanyCsvExport, "Single company CSV export")

        val headers = ExportTestUtils.readCsvHeaders(singleCompanyCsvExport)

        // Check that the null field column DOES exist
        ExportTestUtils.assertColumnPatternExists(
            headers,
            getNullFieldName(),
            shouldExist = true,
            "Single company CSV export with non-null field company",
        )
    }

    /**
     * Tests the CSV export functionality for multiple companies with differing data configurations.
     * The purpose of this test is to ensure that the exported CSV file correctly represents
     * data for multiple companies, maintaining the integrity and accuracy of the data.
     */
    protected fun testCsvExportForBothCompanies() {
        val multiCompanyCsvExport =
            exportDataAsCsv(
                companyIds = listOf(companyWithNullFieldId, companyWithNonNullFieldId),
                reportingPeriods = listOf(reportingPeriod),
            )

        ExportTestUtils.validateExportFile(multiCompanyCsvExport, "Multi-company CSV export")

        val headers = ExportTestUtils.readCsvHeaders(multiCompanyCsvExport)

        validateMultiCompanyExport(multiCompanyCsvExport, headers, "CSV")
    }

    /**
     * Tests the Excel export functionality for two companies with differing data configurations.
     * The purpose of this test is to ensure that the Excel export functionality correctly integrates
     * and represents data for multiple companies, while maintaining data validity and consistency.
     */
    protected fun testExcelExportForBothCompanies() {
        // Export both companies as Excel
        val multiCompanyExcelExport =
            exportDataAsExcel(
                companyIds = listOf(companyWithNullFieldId, companyWithNonNullFieldId),
                reportingPeriods = listOf(reportingPeriod),
            )

        ExportTestUtils.validateExportFile(multiCompanyExcelExport, "Multi-company Excel export")

        // Get the CSV version of the Excel file for analysis
        val excelAsCsvFile = ExportTestUtils.getReadableCsvFile(multiCompanyExcelExport)

        // Read the CSV headers
        val headers = ExportTestUtils.readCsvHeaders(excelAsCsvFile)

        validateMultiCompanyExport(excelAsCsvFile, headers, "Excel")
    }

    /**
     * Tests the CSV export functionality with and without including data meta-information.
     *
     * This method verifies the behavior of the CSV export when the `includeDataMetaInformation`
     * flag is set to `true` and `false`. Specifically, it ensures that:
     * 1. The value of the specified field is present in both exports.
     * 2. Quality-related headers for the specified field are included in the export when
     *    `includeDataMetaInformation` is `true`, but omitted when it is `false`.
     *
     * @param fieldName the name of the field to be validated in the CSV export headers
     */
    protected fun testCsvExportIncludeDataMetaInformationFlag(fieldName: String) {
        // Export data with includeDataMetaInformation=true
        val exportWithMetadata =
            exportDataAsCsvWithMetadata(
                companyIds = listOf(companyWithNonNullFieldId),
                reportingPeriods = listOf(reportingPeriod),
            )

        // Export data with default includeDataMetaInformation=false
        val exportWithoutMetadata =
            exportDataAsCsv(
                companyIds = listOf(companyWithNonNullFieldId),
                reportingPeriods = listOf(reportingPeriod),
            )

        // Validate export files
        ExportTestUtils.validateExportFile(exportWithMetadata, "CSV export with metadata")
        ExportTestUtils.validateExportFile(exportWithoutMetadata, "CSV export without metadata")

        // Read CSV headers from both exports
        val headersWithMetadata =
            ExportTestUtils.readCsvHeaders(
                ExportTestUtils.getReadableCsvFile(exportWithMetadata),
            )
        val headersWithoutMetadata =
            ExportTestUtils.readCsvHeaders(
                ExportTestUtils.getReadableCsvFile(exportWithoutMetadata),
            )

        // 1. Verify that the field with a value appears in both exports
        val valuePattern = "$fieldName.value"
        ExportTestUtils.assertColumnPatternExists(
            headers = headersWithMetadata,
            columnNamePart = valuePattern,
            shouldExist = true,
            contextMessage = "CSV export with includeDataMetaInformation=true should include value of test field",
        )
        ExportTestUtils.assertColumnPatternExists(
            headers = headersWithoutMetadata,
            columnNamePart = valuePattern,
            shouldExist = true,
            contextMessage = "CSV export with includeDataMetaInformation=false should include value of test field",
        )

        // 2. Verify that for the export without DataMetaInformation NO quality headers exist for this field
        val qualityPattern = "$fieldName.quality"
        ExportTestUtils.assertColumnPatternExists(
            headers = headersWithMetadata,
            columnNamePart = qualityPattern,
            shouldExist = true,
            contextMessage = "CSV export with includeDataMetaInformation=true should include quality of test field",
        )
        ExportTestUtils.assertColumnPatternExists(
            headers = headersWithoutMetadata,
            columnNamePart = qualityPattern,
            shouldExist = false,
            contextMessage = "CSV export with includeDataMetaInformation=false should NOT include quality of test field",
        )
    }

    /**
     * Validates the content of a multi-company export file by ensuring that required columns
     * are present and verifying the data consistency across companies.
     *
     * @param exportFile The file containing the exported data for multiple companies.
     * @param headers The list of column headers extracted from the export file to validate against expected columns.
     * @param exportType The type of export being validated, used for contextual messaging and validation.
     */
    private fun validateMultiCompanyExport(
        exportFile: File,
        headers: List<String>,
        exportType: String,
    ) {
        // Verify required columns exist
        val nullFieldColumnIndex =
            ExportTestUtils.assertColumnPatternExists(
                headers,
                getNullFieldName(),
                shouldExist = true,
                "Multi-company $exportType export",
            )

        val companyLeiColumnIndex =
            ExportTestUtils.assertColumnPatternExists(
                headers,
                "companyLei",
                shouldExist = true,
                "Multi-company $exportType export",
            )

        // Read data and validate values
        val companyData =
            ExportTestUtils.readCsvDataByCompanyLei(
                exportFile,
                companyLeiColumnIndex,
                nullFieldColumnIndex,
            )

        ExportTestUtils.validateCompanyData(
            companyData,
            companyWithNullFieldLei,
            companyWithNonNullFieldLei,
            exportType,
        )
    }
}
