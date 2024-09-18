package org.dataland.datalanddataexporter.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandapikeymanager.DatalandDataExporter
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.SfdrDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataSfdrData
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.QaStatus
import org.dataland.datalandbackend.openApiClient.model.SfdrData
import org.dataland.datalandbackend.openApiClient.model.SfdrGeneral
import org.dataland.datalandbackend.openApiClient.model.SfdrGeneralGeneral
import org.dataland.datalandbackend.openApiClient.model.SfdrGeneralGeneralFiscalYearDeviationOptions
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.boot.test.context.SpringBootTest
import java.io.File
import java.time.LocalDate

@SpringBootTest(classes = [DatalandDataExporter::class])
class CsvExporterTest() {
    private lateinit var csvDataExporter: CsvExporter
    private lateinit var mockMetadataControllerApi: MetaDataControllerApi
    private lateinit var mockSfdrDataControllerApi: SfdrDataControllerApi
    private lateinit var mockCompanyDataControllerApi: CompanyDataControllerApi

    val mockMetaData = listOf(
        DataMetaInformation(
            dataType = DataTypeEnum.sfdr,
            reportingPeriod = "2021",
            companyId = "mockCompanyId",
            dataId = "mockDataId",
            qaStatus = QaStatus.Accepted,
            uploadTime = 1,
            currentlyActive = true,
        ),
    )

    // ToDo fake fixture laden?
    private final val mockSfdrData = SfdrData(
        general = SfdrGeneral(
            SfdrGeneralGeneral(
                dataDate = LocalDate.parse("2021-01-01"),
                fiscalYearEnd = LocalDate.parse("2021-01-01"),
                fiscalYearDeviation = SfdrGeneralGeneralFiscalYearDeviationOptions.Deviation,
            ),
        ),
    )

    val mockCompanyAssociatedSfdrData = CompanyAssociatedDataSfdrData(
        companyId = "mockCompanyId",
        reportingPeriod = "2021",
        data = mockSfdrData,
    )

    fun setupMockMetaDataControllerApi(): MetaDataControllerApi {
        val mockMetaDataControllerApi = mock(MetaDataControllerApi::class.java)
        `when`(
            mockMetaDataControllerApi.getListOfDataMetaInfo(
                dataType = any(),
                reportingPeriod = any(),
                companyId = any(),
                showOnlyActive = any(),
            ),
        ).thenReturn(mockMetaData)
        return mockMetaDataControllerApi
    }

    fun setupMockSfdrDataControllerApi(): SfdrDataControllerApi {
        val mockSfdrDataControllerApi = mock(SfdrDataControllerApi::class.java)
        `when`(
            mockSfdrDataControllerApi.getCompanyAssociatedSfdrData(
                dataId = any() ?: "mockDataId",
            ),
        ).thenReturn(mockCompanyAssociatedSfdrData)
        return mockSfdrDataControllerApi
    }

    val mockStoredCompany = StoredCompany(
        companyId = "mockCompanyId",
        companyInformation = CompanyInformation(
            companyName = "mockCompanyName",
            identifiers = mapOf(
                "lei" to listOf("mockLei"),
                "isin" to listOf("mockIsin1", "mockIsin2"),
            ),
            sector = "mockSector",
            countryCode = "mockCountryCode",
            headquarters = "mockHeadquarters",
        ),
        dataRegisteredByDataland = emptyList(),
    )

    fun setupMockCompanyDataControllerApi(): CompanyDataControllerApi {
        val mockCompanyDataControllerApi = mock(CompanyDataControllerApi::class.java)
        `when`(
            mockCompanyDataControllerApi.getCompanyById(
                companyId = any() ?: "mockCompanyId",
            ),
        ).thenReturn(mockStoredCompany)
        return mockCompanyDataControllerApi
    }

    @BeforeEach
    fun setup() {
        mockMetadataControllerApi = setupMockMetaDataControllerApi()
        mockSfdrDataControllerApi = setupMockSfdrDataControllerApi()
        mockCompanyDataControllerApi = setupMockCompanyDataControllerApi()

        csvDataExporter = CsvExporter(
            metaDataControllerApi = mockMetadataControllerApi,
            sfdrDataControllerApi = mockSfdrDataControllerApi,
            companyDataControllerApi = mockCompanyDataControllerApi,
            outputDirectory = "./src/test/resources/csv/output",
        )
    }

    // val inputJson = this.javaClass.classLoader.getResourceAsStream("./src/test/resources/csv/input.json")
    val inputJson = File("./src/test/resources/csv/input.json")

    val expectedTransformationRules = mapOf(
        "presentMapping" to "presentHeader",
        "notMapped" to "",
        "mappedButNoData" to "mappedButNoDataHeader",
        "nested.nestedMapping" to "nestedHeader",
    )
    val expectedCsvData =
        mapOf("presentHeader" to "Here", "mappedButNoDataHeader" to "", "nestedHeader" to "NestedHere")

    @Test
    fun `check that mapJsonToCsv returns correct csv data`() {
        val jsonNode = ObjectMapper().readTree(inputJson)
        val csvData = csvDataExporter.mapJsonToCsv(jsonNode, expectedTransformationRules)
        Assertions.assertEquals(expectedCsvData, csvData)
    }

    @Test
    fun `check that running the sfdr export does not throw an error`() {
        assertDoesNotThrow { csvDataExporter.exportAllSfdrData() }
    }
}
