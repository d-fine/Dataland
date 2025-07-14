package org.dataland.datalandbackend.services

import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.dataland.datalandbackend.frameworks.lksg.model.LksgData
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.enums.data.QualityOptions
import org.dataland.datalandbackend.model.export.SingleCompanyExportData
import org.dataland.datalandbackend.utils.DataPointUtils
import org.dataland.datalandbackend.utils.ReferencedReportsUtilities
import org.dataland.datalandbackend.utils.TestDataProvider
import org.dataland.datalandbackendutils.model.ExportFileType
import org.dataland.datalandbackendutils.utils.JsonUtils
import org.dataland.specificationservice.openApiClient.model.FrameworkSpecification
import org.dataland.specificationservice.openApiClient.model.IdWithRef
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID

const val TEST_ALIAS_1 = "TEST_ALIAS_1"
const val LARGE_DECIMAL = 1234567899.1
const val LARGE_DECIMAL_AS_STRING = "1234567899.1"
const val TEST_COMPANY_NAME = "Test Company"
const val TEST_COMPANY_LEI = "TEST67890"
const val TEST_CATEGORY = "testCategory"
const val TEST_DATA_POINT_NAME_FIRST_IN_ALPHABET = "aTestDataPoint"
const val TEST_DATA_POINT_NAME = "testDataPoint"
const val VALUE_STRING = "value"
const val QUALITY_STRING = "quality"

class DataExportServiceTest {
    private val objectMapper = JsonUtils.defaultObjectMapper
    private val mockDataPointUtils = mock<DataPointUtils>()
    private val mockReferencedReportsUtils = mock<ReferencedReportsUtilities>()
    private val dataExportUtils = DataExportUtils(mockDataPointUtils, mockReferencedReportsUtils)
    private val dataExportService = DataExportService(dataExportUtils)

    private val testDataProvider = TestDataProvider(objectMapper)
    private val lksgTestData = testDataProvider.getLksgDataset()
    private val testReportingPeriod = "2024"
    private val lksgCompanyExportTestData =
        SingleCompanyExportData(
            companyName = "test name",
            companyLei = UUID.randomUUID().toString(),
            reportingPeriod = testReportingPeriod,
            data = lksgTestData,
        )

    private val companyExportDataLksgInputFile = "./src/test/resources/dataExport/lksgDataInput.json"
    private val companyExportDataLksgTestData =
        objectMapper.readValue<SingleCompanyExportData<LksgData>>(File(companyExportDataLksgInputFile))

    @Test
    fun `minimal test for writing excel file`() {
        // Define constants for headers
        val header1 = "Header 1"
        val header2 = "Header 2"
        val header3 = "Header 3"

        val header = listOf(header1, header2, header3)
        val csvSchemaBuilder = CsvSchema.builder()
        header.forEach { column ->
            csvSchemaBuilder.addColumn(column)
        }
        val csvSchema = csvSchemaBuilder.build().withHeader()

        val data =
            listOf(
                mapOf(header1 to "Row 1 Col 1", header2 to "Row 1 Col 2", header3 to "Row 1 Col 3"),
                mapOf(header1 to "Row 2 Col 1", header2 to "Row 2 Col 2", header3 to "Row 2 Col 3"),
            )

        Assertions.assertDoesNotThrow {
            dataExportService.transformDataToExcelWithReadableHeaders(data, csvSchema, ByteArrayOutputStream())
        }
    }

    @Test
    fun `check that exported json coincides with input object`() {
        val jsonStream =
            dataExportService.buildStreamFromPortfolioExportData(
                listOf(lksgCompanyExportTestData),
                ExportFileType.JSON,
                DataType.valueOf("lksg"),
                keepValueFieldsOnly = true,
                includeAliases = false,
            )
        val exportedJsonObject = objectMapper.readValue<List<SingleCompanyExportData<LksgData>>>(jsonStream.inputStream)

        Assertions.assertEquals(listOf(lksgCompanyExportTestData), exportedJsonObject)
    }

    @Test
    fun `check that exported csv coincides with predefined output for lksg`() {
        val csvStream =
            dataExportService.buildStreamFromPortfolioExportData(
                listOf(companyExportDataLksgTestData),
                ExportFileType.CSV,
                DataType.valueOf("lksg"),
                keepValueFieldsOnly = true,
                includeAliases = false,
            )
        val csvString = String(csvStream.inputStream.readAllBytes(), Charsets.UTF_8)
        val predefinedCsv = File("./src/test/resources/dataExport/lksgDataOutput.csv").readText(Charsets.UTF_8)

        Assertions.assertEquals(predefinedCsv, csvString)
    }

    @Test
    fun `check that exported Excel contains expected data`() {
        val excelStream =
            dataExportService.buildStreamFromPortfolioExportData(
                listOf(companyExportDataLksgTestData),
                ExportFileType.EXCEL,
                DataType.valueOf("lksg"),
                keepValueFieldsOnly = true,
                includeAliases = false,
            )
        val bytes = excelStream.inputStream.readAllBytes()
        Assertions.assertTrue(bytes.isNotEmpty(), "Excel stream should not be empty")

        val inputStream = ByteArrayInputStream(bytes)
        assertDoesNotThrow {
            val workbook = XSSFWorkbook(inputStream)
            val sheet = workbook.getSheetAt(0)
            val headerRow = sheet.getRow(0)
            Assertions.assertTrue(workbook.numberOfSheets > 0, "Excel should have at least one sheet")
            Assertions.assertNotNull(headerRow, "Header row should exist")
            Assertions.assertTrue(
                headerRow.getCell(0)?.stringCellValue?.isNotEmpty() ?: false,
                "First header cell should have content",
            )
            workbook.close()
        }
    }

    @Test
    fun `check that quality field is used as value when no value field exists and keepValueFieldsOnly is true`() {
        val testJson = testDataProvider.createTestJsonWithQualityNullValue()

        // Process the data with keepValueFieldsOnly = true
        val companyExportData =
            SingleCompanyExportData(
                companyName = "Quality Test Company",
                companyLei = TEST_COMPANY_LEI,
                reportingPeriod = testReportingPeriod,
                data = objectMapper.treeToValue(testJson, Any::class.java),
            )

        val csvStream =
            dataExportService.buildStreamFromPortfolioExportData(
                listOf(companyExportData),
                ExportFileType.CSV,
                DataType.valueOf("lksg"),
                keepValueFieldsOnly = true,
                includeAliases = false,
            )

        val csvString = String(csvStream.inputStream.readAllBytes(), Charsets.UTF_8)

        // Verify that the quality value was correctly used as a value field
        // We expect to find "Audited" in the CSV (our test quality value)
        Assertions.assertTrue(
            csvString.contains(QualityOptions.Audited.toString()),
            "CSV should contain the quality value 'Audited' in place of missing value field",
        )

        // Check that the original quality field name is not present in the output
        val separator = JsonUtils.getPathSeparator()
        Assertions.assertFalse(
            csvString.contains("${separator}$QUALITY_STRING"),
            "CSV should not contain the original quality field",
        )
    }

    @Test
    fun `check that quality field is ignored when value field exists and keepValueFieldsOnly is true`() {
        val testJson = testDataProvider.createTestJsonWithBothValueAndQuality()

        // Process the data with keepValueFieldsOnly = true
        val companyExportData =
            SingleCompanyExportData(
                companyName = "Both Fields Test Company",
                companyLei = TEST_COMPANY_LEI,
                reportingPeriod = testReportingPeriod,
                data = objectMapper.treeToValue(testJson, Any::class.java),
            )

        val csvStream =
            dataExportService.buildStreamFromPortfolioExportData(
                listOf(companyExportData),
                ExportFileType.CSV,
                DataType.valueOf("lksg"),
                keepValueFieldsOnly = true,
                includeAliases = true,
            )

        val csvString = String(csvStream.inputStream.readAllBytes(), Charsets.UTF_8)

        // Verify that the value field was kept (and not the quality field)
        Assertions.assertTrue(
            csvString.contains("42"),
            "CSV should contain the value field content '42'",
        )

        // Verify that the quality field content is not present in the CSV
        Assertions.assertFalse(
            csvString.contains(QualityOptions.Reported.toString()),
            "CSV should not contain the quality value 'Reported' when value exists",
        )
    }

    @Test
    fun `check that the exported columns are ordered according to the specification`() {
        val testJsonWithOneValue = testDataProvider.createTestJsonWithBothValueAndQuality()
        val testJsonWithTwoValues = testDataProvider.createTestJsonWithTwoDataPoints()
        whenever(mockDataPointUtils.getFrameworkSpecificationOrNull("lksg")).thenReturn(
            FrameworkSpecification(
                IdWithRef("testId", "testRef"),
                "testFramework",
                "testBusinessefinition",
                testDataProvider.createTestSpecification(),
                "testPath",
            ),
        )

        val csvStream =
            dataExportService.buildStreamFromPortfolioExportData(
                listOf(
                    SingleCompanyExportData(
                        companyName = "Test Company 1",
                        companyLei = TEST_COMPANY_LEI,
                        reportingPeriod = testReportingPeriod,
                        data = objectMapper.treeToValue(testJsonWithOneValue, Any::class.java),
                    ),
                    SingleCompanyExportData(
                        companyName = "Test Company 2",
                        companyLei = TEST_COMPANY_LEI,
                        reportingPeriod = testReportingPeriod,
                        data = objectMapper.treeToValue(testJsonWithTwoValues, Any::class.java),
                    ),
                ),
                ExportFileType.CSV,
                DataType.valueOf("lksg"),
                keepValueFieldsOnly = true,
                includeAliases = false,
            )

        val csvString = String(csvStream.inputStream.readAllBytes(), Charsets.UTF_8)
        // check that columns are not ordered alphabetically, but according to the specification
        val colsInCorrectOrder =
            """"data.$TEST_CATEGORY.$TEST_DATA_POINT_NAME.$VALUE_STRING"""" +
                ""","data.$TEST_CATEGORY.$TEST_DATA_POINT_NAME_FIRST_IN_ALPHABET.$VALUE_STRING""""
        Assertions.assertTrue(
            csvString.contains(colsInCorrectOrder),
            "CSV does not contain the specified columns in the correct order",
        )
    }

    @Test
    fun `check that the specified aliases are exported`() {
        val testJson = testDataProvider.createTestJsonWithBothValueAndQuality()

        whenever(mockDataPointUtils.getFrameworkSpecificationOrNull("sfdr")).thenReturn(
            FrameworkSpecification(
                IdWithRef("testId", "testRef"),
                "testFramework",
                "testBusinessefinition",
                testDataProvider.createTestSpecification(),
                "testPath",
            ),
        )

        val csvStream =
            dataExportService.buildStreamFromPortfolioExportData(
                listOf(
                    SingleCompanyExportData(
                        companyName = TEST_COMPANY_NAME,
                        companyLei = TEST_COMPANY_LEI,
                        reportingPeriod = testReportingPeriod,
                        data = objectMapper.treeToValue(testJson, Any::class.java),
                    ),
                ),
                ExportFileType.CSV,
                DataType.valueOf("sfdr"),
                keepValueFieldsOnly = true,
                includeAliases = true,
            )

        val csvString = String(csvStream.inputStream.readAllBytes(), Charsets.UTF_8)

        Assertions.assertTrue(
            csvString.contains(TEST_ALIAS_1),
            "CSV does not contain the export alias $TEST_ALIAS_1",
        )
    }

    @Test
    fun `check that large decimals are exported properly and not in scientific notation`() {
        val testJson = testDataProvider.createTestJsonWithLargeDecimal()

        val csvStream =
            dataExportService.buildStreamFromPortfolioExportData(
                listOf(
                    SingleCompanyExportData(
                        companyName = TEST_COMPANY_NAME,
                        companyLei = TEST_COMPANY_LEI,
                        reportingPeriod = testReportingPeriod,
                        data = objectMapper.treeToValue(testJson, Any::class.java),
                    ),
                ),
                ExportFileType.CSV,
                DataType.valueOf("sfdr"),
                keepValueFieldsOnly = true,
                includeAliases = true,
            )

        val csvString = String(csvStream.inputStream.readAllBytes(), Charsets.UTF_8)

        Assertions.assertTrue(
            csvString.contains(LARGE_DECIMAL_AS_STRING),
            "CSV does not contain the large decimal as string $LARGE_DECIMAL_AS_STRING",
        )
    }

    @Test
    fun `test custom components do not automatically export the data quality when values are available`() {
        val testJson = testDataProvider.createTestJsonNonPrimitiveValue()

        val csvStream =
            dataExportService.buildStreamFromPortfolioExportData(
                listOf(
                    SingleCompanyExportData(
                        companyName = TEST_COMPANY_NAME,
                        companyLei = TEST_COMPANY_LEI,
                        reportingPeriod = testReportingPeriod,
                        data = objectMapper.treeToValue(testJson, Any::class.java),
                    ),
                ),
                ExportFileType.CSV,
                DataType.valueOf("sfdr"),
                keepValueFieldsOnly = true,
                includeAliases = false,
            )

        val csvString = String(csvStream.inputStream.readAllBytes(), Charsets.UTF_8)

        Assertions.assertFalse(
            csvString.contains(QualityOptions.Reported.toString()),
            "CSV should not contain the data quality",
        )
    }
}
