package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.dataland.datalandbackend.frameworks.lksg.model.LksgData
import org.dataland.datalandbackend.model.DataType
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

class DataExportServiceTest {
    private val testAlias2 = "TEST_ALIAS_2"
    private val objectMapper = JsonUtils.defaultObjectMapper
    private val mockDataPointUtils = mock<DataPointUtils>()
    private val mockReferencedReportsUtils = mock<ReferencedReportsUtilities>()
    private val dataExportUtils = DataExportUtils(mockDataPointUtils, mockReferencedReportsUtils)
    private val dataExportService = DataExportService(dataExportUtils)

    private val testDataProvider = TestDataProvider(objectMapper)
    private val lksgTestData = testDataProvider.getLksgDataset()
    private val lksgCompanyExportTestData =
        SingleCompanyExportData(
            companyName = "test name",
            companyLei = UUID.randomUUID().toString(),
            reportingPeriod = "2024",
            data = lksgTestData,
        )

    private val companyExportDataLksgInputFile = "./src/test/resources/dataExport/lksgDataInput.json"
    private val companyExportDataLksgTestData =
        objectMapper
            .readValue<SingleCompanyExportData<LksgData>>(File(companyExportDataLksgInputFile))

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
        val testJson = createTestJsonWithQualityNoValue()

        // Process the data with keepValueFieldsOnly = true
        val companyExportData =
            SingleCompanyExportData(
                companyName = "Quality Test Company",
                companyLei = "TEST12345",
                reportingPeriod = "2024",
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
            csvString.contains("Audited"),
            "CSV should contain the quality value 'Audited' in place of missing value field",
        )

        // Check that the original quality field name is not present in the output
        val separator = JsonUtils.getPathSeparator()
        Assertions.assertFalse(
            csvString.contains("${separator}quality"),
            "CSV should not contain the original quality field",
        )
    }

    @Test
    fun `check that quality field is ignored when value field exists and keepValueFieldsOnly is true`() {
        val testJson = createTestJsonWithBothValueAndQuality()

        // Process the data with keepValueFieldsOnly = true
        val companyExportData =
            SingleCompanyExportData(
                companyName = "Both Fields Test Company",
                companyLei = "TEST67890",
                reportingPeriod = "2024",
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
            csvString.contains("Reported"),
            "CSV should not contain the quality value 'Reported' when value exists",
        )
    }

    @Test
    fun `check that the exported columns are ordered according to the specification`() {
        val testJsonWithOneValue = createTestJsonWithBothValueAndQuality()
        val testJsonWithTwoValues = createTestJsonWithTwoDataPoints()
        whenever(mockDataPointUtils.getFrameworkSpecificationOrNull("lksg")).thenReturn(
            FrameworkSpecification(
                IdWithRef("testId", "testRef"),
                "testFramework",
                "testBusinessefinition",
                createTestSpecification(),
                "testPath",
            ),
        )

        val csvStream =
            dataExportService.buildStreamFromPortfolioExportData(
                listOf(
                    SingleCompanyExportData(
                        companyName = "Test Company 1",
                        companyLei = "TEST67890",
                        reportingPeriod = "2024",
                        data = objectMapper.treeToValue(testJsonWithOneValue, Any::class.java),
                    ),
                    SingleCompanyExportData(
                        companyName = "Test Company 2",
                        companyLei = "TEST67890",
                        reportingPeriod = "2024",
                        data = objectMapper.treeToValue(testJsonWithTwoValues, Any::class.java),
                    ),
                ),
                ExportFileType.CSV,
                DataType.valueOf("lksg"),
                keepValueFieldsOnly = true,
                includeAliases = false,
            )

        val csvString = String(csvStream.inputStream.readAllBytes(), Charsets.UTF_8)
        val colsInCorrectOrder =
            """"data.testCategory.aTestDataPoint.value","data.testCategory.testDataPoint.value""""
        // Verify that the value field was kept (and not the quality field)
        Assertions.assertTrue(
            csvString
                .contains(colsInCorrectOrder),
            "CSV does not contain the specified columns in the correct order",
        )
    }

    @Test
    fun `check that the specified aliases are exported`() {
        val testJson = createTestJsonWithBothValueAndQuality()

        whenever(mockDataPointUtils.getFrameworkSpecificationOrNull("sfdr")).thenReturn(
            FrameworkSpecification(
                IdWithRef("testId", "testRef"),
                "testFramework",
                "testBusinessefinition",
                createTestSpecification(),
                "testPath",
            ),
        )

        val csvStream =
            dataExportService.buildStreamFromPortfolioExportData(
                listOf(
                    SingleCompanyExportData(
                        companyName = "Test Company ",
                        companyLei = "TEST67890",
                        reportingPeriod = "2024",
                        data = objectMapper.treeToValue(testJson, Any::class.java),
                    ),
                ),
                ExportFileType.CSV,
                DataType.valueOf("sfdr"),
                keepValueFieldsOnly = true,
                includeAliases = true,
            )

        val csvString = String(csvStream.inputStream.readAllBytes(), Charsets.UTF_8)
        // Verify that the value field was kept (and not the quality field)
        Assertions.assertTrue(
            csvString
                .contains(testAlias2),
            "CSV does not contain the export alias $testAlias2",
        )
    }

    /**
     * Creates a test JSON with a data point that has only a quality field (no value field)
     */
    private fun createTestJsonWithQualityNoValue(): JsonNode {
        val root = objectMapper.createObjectNode()

        val testField = objectMapper.createObjectNode()
        root.set<JsonNode>("testCategory", testField)

        val testPoint = objectMapper.createObjectNode()
        testField.set<JsonNode>("testDataPoint", testPoint)

        // Only set a quality field, no value field
        testPoint.put("quality", "Audited")

        return root
    }

    /**
     * Creates a test JSON with a data point that has both a value and quality field
     */
    private fun createTestJsonWithBothValueAndQuality(): JsonNode {
        val root = objectMapper.createObjectNode()

        val testField = objectMapper.createObjectNode()
        root.set<JsonNode>("testCategory", testField)

        val testPoint = objectMapper.createObjectNode()
        testField.set<JsonNode>("testDataPoint", testPoint)

        // Set both value and quality fields
        testPoint.put("value", "42")
        testPoint.put("quality", "Reported")

        return root
    }

    /**
     * Creates a test JSON with a data point that has both a value and quality field
     */
    private fun createTestJsonWithTwoDataPoints(): JsonNode {
        val root = objectMapper.createObjectNode()

        val testField = objectMapper.createObjectNode()
        root.set<JsonNode>("testCategory", testField)

        val testPointA = objectMapper.createObjectNode()
        testField.set<JsonNode>("aTestDataPoint", testPointA)

        testPointA.put("value", "123")
        testPointA.put("quality", "Reported")

        val testPointB = objectMapper.createObjectNode()
        testField.set<JsonNode>("testDataPoint", testPointB)

        testPointB.put("value", "42")
        testPointB.put("quality", "Reported")

        return root
    }

    /**
     * Creates a test specification matching the test data created in createTestJsonWithBothValueAndQuality
     */
    private fun createTestSpecification(): String {
        val root = objectMapper.createObjectNode()

        val testField = objectMapper.createObjectNode()
        root.set<JsonNode>("testCategory", testField)

        val testPointA = objectMapper.createObjectNode()
        testField.set<JsonNode>("aTestDataPoint", testPointA)

        testPointA.put("id", "testId1")
        testPointA.put("ref", "testRef1")
        testPointA.put("aliasExport", "TEST_ALIAS_1")

        val testPointB = objectMapper.createObjectNode()
        testField.set<JsonNode>("testDataPoint", testPointB)

        // Set both value and quality fields
        testPointB.put("id", "testId2")
        testPointB.put("ref", "testRef2")
        testPointB.put("aliasExport", testAlias2)

        return root.toString()
    }
}
