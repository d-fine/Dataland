package org.dataland.datalandbackend.services

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.model.EutaxonomyNonFinancialsData
import org.dataland.datalandbackend.frameworks.lksg.model.LksgData
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.export.SingleCompanyExportData
import org.dataland.datalandbackend.utils.DataPointUtils
import org.dataland.datalandbackend.utils.ReferencedReportsUtilities
import org.dataland.datalandbackend.utils.TestDataProvider
import org.dataland.datalandbackendutils.model.ExportFileType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID

class DataExportServiceTest {
    private val objectMapper = jacksonObjectMapper().findAndRegisterModules()
    private val mockDataPointUtils = mock<DataPointUtils>()
    private val mockReferencedReportsUtils = mock<ReferencedReportsUtilities>()
    private val dataExportService = DataExportService(objectMapper, mockDataPointUtils, mockReferencedReportsUtils)

    private val testDataProvider = TestDataProvider(objectMapper)
    private val euTaxonomyNonFinancialsTestData = testDataProvider.getEuTaxonomyNonFinancialsDataset()
    private val euTaxonomyCompanyExportTestData =
        SingleCompanyExportData(
            companyName = "test name",
            companyLei = UUID.randomUUID().toString(),
            reportingPeriod = "2024",
            data = euTaxonomyNonFinancialsTestData,
        )

    private val companyExportDataLksgInputFile = "./src/test/resources/dataExport/lksgDataInput.json"
    private val companyExportDataLksgTestData =
        objectMapper
            .readValue<SingleCompanyExportData<LksgData>>(File(companyExportDataLksgInputFile))

    @Test
    fun `minimal test for writing excel file`() {
        val header = listOf("Header 1", "Header 2", "Header 3")
        val data =
            listOf(
                mapOf("Header 1" to "Row 1 Col 1", "Header 2" to "Row 1 Col 2", "Header 3" to "Row 1 Col 3"),
                mapOf("Header 1" to "Row 2 Col 1", "Header 2" to "Row 2 Col 2", "Header 3" to "Row 2 Col 3"),
            )

        Assertions.assertDoesNotThrow {
            dataExportService.transformDataToExcel(header, data, ByteArrayOutputStream())
        }
    }

    @Test
    fun `check that exported json coincides with input object`() {
        val jsonStream =
            dataExportService.buildStreamFromPortfolioExportData(
                listOf(euTaxonomyCompanyExportTestData),
                ExportFileType.JSON,
                DataType.valueOf("eutaxonomy-non-financials"),
                keepValueFieldsOnly = true,
            )
        val exportedJsonObject = objectMapper.readValue<List<SingleCompanyExportData<EutaxonomyNonFinancialsData>>>(jsonStream.inputStream)

        Assertions.assertEquals(listOf(euTaxonomyCompanyExportTestData), exportedJsonObject)
    }

    @Test
    fun `check that exported csv coincides with predefined output`() {
        val csvStream =
            dataExportService.buildStreamFromPortfolioExportData(
                listOf(companyExportDataLksgTestData),
                ExportFileType.CSV,
                DataType.valueOf("lksg"),
                keepValueFieldsOnly = true,
            )
        val csvString = String(csvStream.inputStream.readAllBytes(), Charsets.UTF_8)
        val predefinedCsv = File("./src/test/resources/dataExport/lksgDataOutput.csv").inputStream().readAllBytes().toString(Charsets.UTF_8)

        Assertions.assertEquals(predefinedCsv, csvString)
    }

    @Test
    fun `check that exported Excel starts with declaration of separator`() {
        val excelStream =
            dataExportService.buildStreamFromPortfolioExportData(
                listOf(companyExportDataLksgTestData),
                ExportFileType.EXCEL,
                DataType.valueOf("lksg"),
                keepValueFieldsOnly = true,
            )
        val csvString = String(excelStream.inputStream.readAllBytes(), Charsets.UTF_8)

        Assertions.assertTrue(csvString.matches("^.?sep=,.*$".toRegex()))
    }
}
