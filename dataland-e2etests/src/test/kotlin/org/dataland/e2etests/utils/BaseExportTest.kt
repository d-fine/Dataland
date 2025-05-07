package org.dataland.e2etests.utils

import org.awaitility.Awaitility
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import java.io.File
import java.util.concurrent.TimeUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseExportTest<T> {
    protected val apiAccessor = ApiAccessor()

    // Common properties that all export tests need
    protected lateinit var companyWithNullFieldId: String
    protected lateinit var companyWithNonNullFieldId: String
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
    ): File

    protected abstract fun exportDataAsCsvWithMetadata(
        companyIds: List<String>,
        reportingPeriods: List<String>,
    ): File

    protected abstract fun exportDataAsExcel(
        companyIds: List<String>,
        reportingPeriods: List<String>,
    ): File

    // Setup method template - subclasses can call this in their @BeforeAll method
    protected fun setupCompaniesAndData() {
        // Create test companies
        companyWithNullFieldId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        companyWithNonNullFieldId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId

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
        ExportTestUtils.assertColumnExists(
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
        ExportTestUtils.assertColumnExists(
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

    protected fun testCsvExportWithMetadataIncludesMetaInformationFields() {
        // Export data with includeMetaData=true
        val exportWithMetadata =
            exportDataAsCsvWithMetadata(
                companyIds = listOf(companyWithNonNullFieldId),
                reportingPeriods = listOf(reportingPeriod),
            )

        // Export data with default includeMetaData=false
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

        val metadataColumn = "companyId"
        val dataColumn = "dataDate"

        // Verify the presence/absence of metadata fields
        ExportTestUtils.assertColumnExists(
            headers = headersWithMetadata,
            columnName = metadataColumn,
            shouldExist = true,
            contextMessage = "CSV export with includeMetaData=true",
        )

        ExportTestUtils.assertColumnExists(
            headers = headersWithoutMetadata,
            columnName = metadataColumn,
            shouldExist = false,
            contextMessage = "CSV export with includeMetaData=false",
        )

        // Verify that regular data fields are present in both exports
        ExportTestUtils.assertColumnExists(
            headers = headersWithMetadata,
            columnName = dataColumn,
            shouldExist = true,
            contextMessage = "CSV export with includeMetaData=true",
        )

        ExportTestUtils.assertColumnExists(
            headers = headersWithoutMetadata,
            columnName = dataColumn,
            shouldExist = true,
            contextMessage = "CSV export with includeMetaData=false",
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
            ExportTestUtils.assertColumnExists(
                headers,
                getNullFieldName(),
                shouldExist = true,
                "Multi-company $exportType export",
            )

        val companyIdColumnIndex =
            ExportTestUtils.assertColumnExists(
                headers,
                "companyId",
                shouldExist = true,
                "Multi-company $exportType export",
            )

        // Read data and validate values
        val companyData =
            ExportTestUtils.readCsvDataByCompanyId(
                exportFile,
                companyIdColumnIndex,
                nullFieldColumnIndex,
            )

        ExportTestUtils.validateCompanyData(
            companyData,
            companyWithNullFieldId,
            companyWithNonNullFieldId,
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
