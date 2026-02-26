package org.dataland.e2etests.utils

import org.dataland.datalandbackend.openApiClient.model.ExportFileType
import org.dataland.datalandbackend.openApiClient.model.ExportLatestRequestData
import org.dataland.datalandbackend.openApiClient.model.ExportRequestData
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.TestInstance
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseExportTest<T> : BaseExportTestSetup<T>() {
    // Common test implementations that can be called from subclasses

    /**
     * Verifies that the CSV export excludes a column corresponding to a null field in the test data.
     * The test ensures that null fields in the source data are properly omitted from the exported CSV file.
     */
    protected fun testCsvExportOmitsColumnForNullField() {
        // Export only the company with a null field
        val singleCompanyCsvExport =
            exportDataAsCsv(
                companyIds = listOf(companyWithNullFieldId),
                reportingPeriods = listOf(reportingPeriod),
            )

        ExportTestUtils.validateExportFile(singleCompanyCsvExport, "Single company CSV export")

        val headers = ExportTestUtils.readCsvHeaders(singleCompanyCsvExport)

        // Check that the null field column does NOT exist
        ExportTestUtils.assertColumnPatternExistsOrNot(
            headers,
            getNullFieldName(),
            shouldExist = false,
            "Single company CSV export with null field company",
        )
    }

    protected fun testCsvExportIncludesColumnForNonNullField() {
        // Export only the company with a non-null field
        val singleCompanyCsvExport =
            exportDataAsCsv(
                companyIds = listOf(companyWithNonNullFieldId),
                reportingPeriods = listOf(reportingPeriod),
            )

        ExportTestUtils.validateExportFile(singleCompanyCsvExport, "Single company CSV export")

        val headers = ExportTestUtils.readCsvHeaders(singleCompanyCsvExport)

        // Check that the null field column DOES exist
        ExportTestUtils.assertColumnPatternExistsOrNot(
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
        frameworkSpecificValidationForCSVExportBothCompanies(headers)
    }

    /**
     * Tests the Excel export functionality for two companies with differing data configurations.
     * The purpose of this test is to ensure that the Excel export functionality correctly integrates
     * and represents data for multiple companies while maintaining data validity and consistency.
     */
    protected fun testExcelExportForBothCompanies() {
        // Export both companies as Excel
        val multiCompanyExcelExport =
            changeFilenameToEndWithXlsx(
                exportDataAsExcel(
                    companyIds = listOf(companyWithNullFieldId, companyWithNonNullFieldId),
                    reportingPeriods = listOf(reportingPeriod),
                ),
            )
        ExportTestUtils.validateExportFile(multiCompanyExcelExport, "Multi-company Excel export")

        // Get the CSV version of the Excel file for analysis
        val excelAsCsvFile = ExportTestUtils.getReadableCsvFile(multiCompanyExcelExport)

        // Read the CSV headers
        val headers = ExportTestUtils.readCsvHeaders(excelAsCsvFile)

        validateMultiCompanyExport(excelAsCsvFile, headers, "Excel")
    }

    private fun changeFilenameToEndWithXlsx(multiCompanyExcelExport: File): File {
        val tmp = multiCompanyExcelExport
        val excelFilename = "${multiCompanyExcelExport.parent}\\${multiCompanyExcelExport.nameWithoutExtension}.xlsx"

        return if (tmp.renameTo(File(excelFilename))) {
            File(excelFilename)
        } else {
            multiCompanyExcelExport
        }
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
            exportDataAsCsv(
                companyIds = listOf(companyWithNonNullFieldId),
                reportingPeriods = listOf(reportingPeriod),
                keepValueFieldsOnly = false,
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
        ExportTestUtils.assertColumnPatternExistsOrNot(
            headers = headersWithMetadata,
            columnNamePart = valuePattern,
            shouldExist = true,
            contextMessage = "CSV export with includeDataMetaInformation=true should include value of test field",
        )
        ExportTestUtils.assertColumnPatternExistsOrNot(
            headers = headersWithoutMetadata,
            columnNamePart = valuePattern,
            shouldExist = true,
            contextMessage = "CSV export with includeDataMetaInformation=false should include value of test field",
        )

        // 2. Verify that for the export without DataMetaInformation NO quality headers exist for this field
        val qualityPattern = "$fieldName.quality"
        ExportTestUtils.assertColumnPatternExistsOrNot(
            headers = headersWithMetadata,
            columnNamePart = qualityPattern,
            shouldExist = true,
            contextMessage = "CSV export with includeDataMetaInformation=true should include quality of test field",
        )
        ExportTestUtils.assertColumnPatternExistsOrNot(
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
            ExportTestUtils.assertColumnPatternExistsOrNot(
                headers,
                getNullFieldName(),
                shouldExist = true,
                "Multi-company $exportType export",
            )

        val companyLeiColumnIndex =
            ExportTestUtils.assertColumnPatternExistsOrNot(
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

    /**
     * This method is intentionally left empty.
     * It serves as a placeholder for future implementation in subclasses.
     */
    open fun frameworkSpecificValidationForCSVExportBothCompanies(headers: List<String>) {
        // This function can be overwritten in the export tests for the specific frameworks to test cases specific
        // to those frameworks, e.g., the correct export of the activities arrays
        // in the eutaxonomy non financials framework
    }

    /**
     * Tests the CSV export functionality with and without including alias information.
     *
     * This method verifies the behavior of the CSV export when the `includeAlias` flag is set to `true` and `false`.
     * Specifically, it ensures that:
     * 1. The alias value appears in the CSV headers when `includeAlias` is `true`.
     * 2. The alias value is omitted from the CSV headers when `includeAlias` is `false`.
     *
     * @param alias the alias string to be validated in the CSV export headers
     */
    protected fun testCsvExportIncludeAliasFlag(alias: String) {
        // Export data with includeAlias=true
        val exportWithAlias =
            exportDataAsCsv(
                companyIds = listOf(companyWithNonNullFieldId),
                reportingPeriods = listOf(reportingPeriod),
                includeAliases = true,
            )

        // Export data with default includeAlias=false
        val exportWithoutAlias =
            exportDataAsCsv(
                companyIds = listOf(companyWithNonNullFieldId),
                reportingPeriods = listOf(reportingPeriod),
            )

        // Validate export files
        ExportTestUtils.validateExportFile(exportWithAlias, "CSV export with alias")
        ExportTestUtils.validateExportFile(exportWithoutAlias, "CSV export without alias")

        // Read CSV headers from both exports
        val headersWithAlias =
            ExportTestUtils.readCsvHeaders(
                ExportTestUtils.getReadableCsvFile(exportWithAlias),
            )
        val headersWithoutAlias =
            ExportTestUtils.readCsvHeaders(
                ExportTestUtils.getReadableCsvFile(exportWithoutAlias),
            )

        ExportTestUtils.assertColumnPatternExistsOrNot(
            headers = headersWithAlias,
            columnNamePart = alias,
            shouldExist = true,
            contextMessage = "CSV export with includeAlias=true should include value of test field",
        )
        ExportTestUtils.assertColumnPatternExistsOrNot(
            headers = headersWithoutAlias,
            columnNamePart = alias,
            shouldExist = false,
            contextMessage = "CSV export with includeAlias=false should not include value of test field",
        )
    }

    protected fun testExportLatest() {
        val companyIds = listOf(companyWithNullFieldId, companyWithNonNullFieldId)
        val latestData =
            exportLatestData(
                ExportLatestRequestData(companyIds, ExportFileType.JSON),
            )
        ExportTestUtils.validateExportFile(latestData, "Latest export")

        val oldData =
            exportData(
                ExportRequestData(
                    companyIds = companyIds,
                    reportingPeriods = listOf((reportingPeriod.toInt() - 1).toString()),
                    fileFormat = ExportFileType.JSON,
                ),
            )
        val newData =
            exportData(
                ExportRequestData(
                    companyIds = companyIds,
                    reportingPeriods = listOf(reportingPeriod),
                    fileFormat = ExportFileType.JSON,
                ),
            )

        val latestText = latestData.readText()
        val oldText = oldData.readText()
        val newText = newData.readText()

        val objectMapper = defaultObjectMapper
        val latestJson = objectMapper.readTree(latestText).sortedBy { it.toString() }
        val newJson = objectMapper.readTree(newText).sortedBy { it.toString() }
        val oldJson = objectMapper.readTree(oldText).sortedBy { it.toString() }

        Assertions.assertEquals(
            newJson,
            latestJson,
            "Latest export does not match new reporting period (structural JSON comparison)",
        )

        Assertions.assertNotEquals(
            latestJson,
            oldJson,
            "Latest export should differ from old data export $latestText != $oldText",
        )
    }
}
