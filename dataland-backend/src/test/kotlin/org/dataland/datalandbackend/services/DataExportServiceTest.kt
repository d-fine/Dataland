package org.dataland.datalandbackend.services

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.model.EutaxonomyNonFinancialsData
import org.dataland.datalandbackend.frameworks.lksg.model.LksgData
import org.dataland.datalandbackend.model.companies.CompanyAssociatedData
import org.dataland.datalandbackend.utils.TestDataProvider
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File
import java.util.UUID

class DataExportServiceTest {
    private val objectMapper = jacksonObjectMapper().findAndRegisterModules()
    private val dataExportService = DataExportService(objectMapper)

    private val testDataProvider = TestDataProvider(objectMapper)
    private val euTaxonomyNonFinancialsTestData = testDataProvider.getEuTaxonomyNonFinancialsDataset()
    private val companyAssociatedEuTaxonomyTestData =
        CompanyAssociatedData(
            companyId = UUID.randomUUID().toString(),
            reportingPeriod = "2024",
            data = euTaxonomyNonFinancialsTestData,
        )

    private val companyAssociatedLksgInputFile = "./src/test/resources/dataExport/lksgDataInput.json"
    private val companyAssociatedLksgTestData =
        objectMapper
            .readValue<CompanyAssociatedData<LksgData>>(File(companyAssociatedLksgInputFile))

    @Test
    fun `check that exported json coincides with input object`() {
        val jsonStream = dataExportService.buildJsonStreamFromCompanyAssociatedData(companyAssociatedEuTaxonomyTestData)
        val exportedJsonObject = objectMapper.readValue<CompanyAssociatedData<EutaxonomyNonFinancialsData>>(jsonStream.inputStream)

        Assertions.assertEquals(companyAssociatedEuTaxonomyTestData, exportedJsonObject)
    }

    @Test
    fun `check that exported csv coincides with predefined output`() {
        val csvStream = dataExportService.buildCsvStreamFromCompanyAssociatedData(companyAssociatedLksgTestData)
        val csvString = String(csvStream.inputStream.readAllBytes(), Charsets.UTF_8)
        val predefinedCsv = File("./src/test/resources/dataExport/lksgDataOutput.csv").inputStream().readAllBytes().toString(Charsets.UTF_8)

        Assertions.assertEquals(predefinedCsv, csvString)
    }

    @Test
    fun `check that exported Excel starts with declaration of separator`() {
        val excelStream = dataExportService.buildExcelStreamFromCompanyAssociatedData(companyAssociatedLksgTestData)
        val csvString = String(excelStream.inputStream.readAllBytes(), Charsets.UTF_8)

        Assertions.assertTrue(csvString.matches("^.?sep=,.*$".toRegex()))
    }
}
