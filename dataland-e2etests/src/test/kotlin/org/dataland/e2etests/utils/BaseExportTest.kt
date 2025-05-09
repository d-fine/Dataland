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
        includeDataMetaInformation: Boolean = false,
    ): File

    private fun exportDataAsCsvWithMetadata(
        companyIds: List<String>,
        reportingPeriods: List<String>,
    ): File = exportDataAsCsv(companyIds, reportingPeriods, includeDataMetaInformation = true)

    protected abstract fun exportDataAsExcel(
        companyIds: List<String>,
        reportingPeriods: List<String>,
    ): File

    /**
     * Upload a company with a random LEI and return a pair of the company ID and the generated LEI.
     */
    private fun uploadCompanyWithRandomLei(): Pair<String, String> {
        val lei = UUID.randomUUID().toString()
        return Pair(apiAccessor.uploadOneCompanyWithIdentifiers(lei = lei)!!.actualStoredCompany.companyId, lei)
    }

    // Setup method template - subclasses can call this in their @BeforeAll method
    protected fun setupCompaniesAndData() {
        // Create test companies
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

    // Common test implementations that can be called from subclasses
    protected fun testCsvExportOmitsColumnForNullField() {
        // Export only the company with null field
        val singleCompanyCsvExport =
            exportDataAsCsv(
                companyIds = listOf(companyWithNullFieldId),
                reportingPeriods = listOf(reportingPeriod),
            )

        ExportTestUtils.validateExportFile(singleCompanyCsvExport, "Single company CSV export")

        // Read CSV file headers
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

        // Read CSV file headers
        val headers = ExportTestUtils.readCsvHeaders(singleCompanyCsvExport)

        // Check that the null field column DOES exist
        ExportTestUtils.assertColumnPatternExists(
            headers,
            getNullFieldName(),
            shouldExist = true,
            "Single company CSV export with non-null field company",
        )
    }

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

    // Helper method for validating multi-company exports
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
}
