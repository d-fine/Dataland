package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.JsonNode
import org.dataland.datalandbackend.frameworks.lksg.model.LksgData
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.export.ExportAvailability
import org.dataland.datalandbackend.model.export.ExportOptions
import org.dataland.datalandbackend.model.export.SingleCompanyExportData
import org.dataland.datalandbackend.services.datapoints.DatasetAssembler
import org.dataland.datalandbackend.utils.TestDataProvider
import org.dataland.datalandbackendutils.model.ExportFileType
import org.dataland.datalandbackendutils.utils.JsonUtils
import org.dataland.specificationservice.openApiClient.model.DataPointBaseTypeResolvedSchema
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Tests for the schema-driven column ordering and alias renaming behavior of [DataExportService] when
 * exporting assembled frameworks (e.g. sfdr). See [DataExportServiceTest] for the basic export format
 * (JSON/CSV/Excel) building tests.
 */
class DataExportServiceColumnOrderingTest {
    private val objectMapper = JsonUtils.defaultObjectMapper
    private val mockDatasetAssembler = mock<DatasetAssembler>()
    private val mockSpecificationService = mock<SpecificationService>()
    private val mockCompanyQueryManager = mock<CompanyQueryManager>()
    private val mockDatasetStorageService = mock<DatasetStorageService>()
    private val mockNonSourceabilityInformationManager = mock<NonSourceabilityInformationManager>()
    private val dataExportService =
        DataExportService<LksgData>(
            mockDatasetAssembler,
            mockSpecificationService,
            mockCompanyQueryManager,
            mockDatasetStorageService,
            mockNonSourceabilityInformationManager,
        )

    private val testDataProvider = TestDataProvider(objectMapper)

    private val portfolioDataTwoCompanies =
        listOf(
            SingleCompanyExportData(
                companyName = "Test Company 1",
                companyLei = TEST_COMPANY_LEI,
                reportingPeriod = TEST_REPORTING_PERIOD,
                availability = ExportAvailability.AVAILABLE,
                data =
                    objectMapper.treeToValue(
                        testDataProvider.createTestJsonWithBothValueAndQuality(),
                        Any::class.java,
                    ),
            ),
            SingleCompanyExportData(
                companyName = "Test Company 2",
                companyLei = TEST_COMPANY_LEI,
                reportingPeriod = TEST_REPORTING_PERIOD,
                availability = ExportAvailability.AVAILABLE,
                data =
                    objectMapper.treeToValue(
                        testDataProvider.createTestJsonWithTwoDataPoints(),
                        Any::class.java,
                    ),
            ),
        )

    /**
     * Sets up a mock schema for testing purposes.
     */
    private fun setupTestSchema() {
        val resolvedSchemaJson: JsonNode =
            objectMapper.readTree(
                """
                {
                  "$TEST_CATEGORY": {
                    "$TEST_DATA_POINT_NAME": { "value": "number" },
                    "$TEST_DATA_POINT_NAME_FIRST_IN_ALPHABET": { "value": "number" }
                  }
                }
                """.trimIndent(),
            )
        val baseTypeSchema =
            mock<DataPointBaseTypeResolvedSchema> {
                on { resolvedSchema } doReturn resolvedSchemaJson
            }
        doReturn(baseTypeSchema)
            .whenever(mockSpecificationService)
            .getResolvedFrameworkSpecification("sfdr")
        doReturn(objectMapper.readTree(testDataProvider.createTestSpecification()))
            .whenever(mockDatasetAssembler)
            .getFrameworkTemplate("sfdr")
        doReturn(true).whenever(mockSpecificationService).isAssembledFramework("sfdr")
    }

    @Test
    fun `check that the exported columns are ordered according to the specification`() {
        setupTestSchema()
        val csvStream =
            dataExportService.buildStreamFromPortfolioExportData(
                portfolioDataTwoCompanies,
                ExportOptions(
                    DataType.valueOf("sfdr"),
                    ExportFileType.CSV,
                    keepValueFieldsOnly = true,
                    includeAliases = false,
                ),
            )

        val csvString = String(csvStream.inputStream.readAllBytes(), Charsets.UTF_8)

        val headerLine = csvString.lineSequence().first()
        val actualHeaders = headerLine.split(",")

        val expectedHeaders =
            listOf(
                "companyName",
                "companyLei",
                "reportingPeriod",
                "availability",
                "\"data.$TEST_CATEGORY.$TEST_DATA_POINT_NAME.value\"",
                "\"data.$TEST_CATEGORY.$TEST_DATA_POINT_NAME_FIRST_IN_ALPHABET.value\"",
            )

        expectedHeaders.forEach {
            Assertions.assertTrue(actualHeaders.contains(it), "Expected column '$it' not found in CSV header")
        }

        val availabilityIndex = actualHeaders.indexOf("availability")
        val index1 = actualHeaders.indexOf("\"data.$TEST_CATEGORY.$TEST_DATA_POINT_NAME.value\"")
        val index2 = actualHeaders.indexOf("\"data.$TEST_CATEGORY.$TEST_DATA_POINT_NAME_FIRST_IN_ALPHABET.value\"")

        Assertions.assertTrue(
            index1 < index2,
            "Expected '${expectedHeaders[4]}' to appear before '${expectedHeaders[5]}'",
        )
        Assertions.assertTrue(
            availabilityIndex < index1,
            "Expected 'availability' to appear before the framework data columns",
        )
    }

    @Test
    fun `check that the specified aliases are exported`() {
        setupTestSchema()
        val csvStream =
            dataExportService.buildStreamFromPortfolioExportData(
                listOf(
                    SingleCompanyExportData(
                        companyName = "Test Company 1",
                        companyLei = TEST_COMPANY_LEI,
                        reportingPeriod = TEST_REPORTING_PERIOD,
                        availability = ExportAvailability.AVAILABLE,
                        data =
                            objectMapper.treeToValue(
                                testDataProvider.createTestJsonWithBothValueAndQuality(),
                                Any::class.java,
                            ),
                    ),
                    SingleCompanyExportData(
                        companyName = "Test Company 2",
                        companyLei = TEST_COMPANY_LEI,
                        reportingPeriod = TEST_REPORTING_PERIOD,
                        availability = ExportAvailability.AVAILABLE,
                        data =
                            objectMapper.treeToValue(
                                testDataProvider.createTestJsonWithTwoDataPoints(),
                                Any::class.java,
                            ),
                    ),
                ),
                ExportOptions(
                    DataType.valueOf("sfdr"),
                    ExportFileType.CSV,
                    keepValueFieldsOnly = true,
                    includeAliases = true,
                ),
            )

        val csvString = String(csvStream.inputStream.readAllBytes(), Charsets.UTF_8)

        Assertions.assertTrue(
            csvString.contains(TEST_ALIAS_1),
            "CSV does not contain the export alias $TEST_ALIAS_1",
        )
        Assertions.assertTrue(
            csvString.contains(TEST_ALIAS_2),
            "CSV does not contain the export alias $TEST_ALIAS_2",
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
                        reportingPeriod = TEST_REPORTING_PERIOD,
                        availability = ExportAvailability.AVAILABLE,
                        data = objectMapper.treeToValue(testJson, Any::class.java),
                    ),
                ),
                ExportOptions(
                    DataType.valueOf("sfdr"),
                    ExportFileType.CSV,
                    keepValueFieldsOnly = true,
                    includeAliases = true,
                ),
            )

        val csvString = String(csvStream.inputStream.readAllBytes(), Charsets.UTF_8)

        Assertions.assertTrue(
            csvString.contains(LARGE_DECIMAL_AS_STRING),
            "CSV does not contain the large decimal as string $LARGE_DECIMAL_AS_STRING",
        )
    }
}
