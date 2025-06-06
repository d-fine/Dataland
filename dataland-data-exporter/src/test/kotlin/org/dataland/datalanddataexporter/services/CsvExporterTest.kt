package org.dataland.datalanddataexporter.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.SfdrDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.infrastructure.ServerException
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataSfdrData
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.QaStatus
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.datalanddataexporter.TestDataProvider
import org.dataland.datalanddataexporter.services.CsvExporter.Companion.MAX_RETRIES
import org.dataland.datalanddataexporter.utils.FileHandlingUtils.readTransformationConfig
import org.dataland.datalanddataexporter.utils.TransformationUtils
import org.dataland.datalanddataexporter.utils.TransformationUtils.ISIN_IDENTIFIER
import org.dataland.datalanddataexporter.utils.TransformationUtils.LEI_IDENTIFIER
import org.dataland.datalanddataexporter.utils.TransformationUtils.convertDataToJson
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import java.io.File
import java.net.SocketTimeoutException

class CsvExporterTest {
    private lateinit var csvDataExporter: CsvExporter
    private lateinit var mockMetadataControllerApi: MetaDataControllerApi
    private lateinit var mockSfdrDataControllerApi: SfdrDataControllerApi
    private lateinit var mockCompanyDataControllerApi: CompanyDataControllerApi

    private val outputDirectory = "./src/test/resources/csv/output"

    private val mockMetaData =
        listOf(
            DataMetaInformation(
                dataType = DataTypeEnum.sfdr,
                reportingPeriod = "2021",
                companyId = "mockCompanyId",
                dataId = "mockDataId",
                qaStatus = QaStatus.Accepted,
                uploadTime = 1,
                currentlyActive = true,
                ref = "test",
            ),
        )

    private val mockCompanyAssociatedSfdrData =
        CompanyAssociatedDataSfdrData(
            companyId = "mockCompanyId",
            reportingPeriod = "2021",
            data = TestDataProvider.getMockSfdrData(),
        )

    private val mockCompanyAssociatedSfdrDataWithNoNullFields =
        CompanyAssociatedDataSfdrData(
            companyId = "mockCompanyId",
            reportingPeriod = "2021",
            data = TestDataProvider.getMockSfdrDataWithNoNullFields(),
        )

    private fun setupMockMetaDataControllerApi(): MetaDataControllerApi {
        val mockMetaDataControllerApi = mock(MetaDataControllerApi::class.java)
        `when`(
            mockMetaDataControllerApi.getListOfDataMetaInfo(
                dataType = DataTypeEnum.sfdr,
            ),
        ).thenReturn(mockMetaData)
        return mockMetaDataControllerApi
    }

    private fun setupMockSfdrDataControllerApi(): SfdrDataControllerApi {
        val mockSfdrDataControllerApi = mock(SfdrDataControllerApi::class.java)
        `when`(
            mockSfdrDataControllerApi.getCompanyAssociatedSfdrDataByDimensions(
                reportingPeriod = any(),
                companyId = any(),
            ),
        ).thenReturn(mockCompanyAssociatedSfdrData)
        return mockSfdrDataControllerApi
    }

    private val mockStoredCompany =
        StoredCompany(
            companyId = "mockCompanyId",
            companyInformation =
                CompanyInformation(
                    companyName = "mockCompanyName",
                    identifiers =
                        mapOf(
                            LEI_IDENTIFIER to listOf("mockLei"),
                            ISIN_IDENTIFIER to listOf("mockIsin1", "mockIsin2"),
                        ),
                    sector = "mockSector",
                    countryCode = "mockCountryCode",
                    headquarters = "mockHeadquarters",
                ),
            dataRegisteredByDataland = emptyList(),
        )

    private fun setupMockCompanyDataControllerApi(): CompanyDataControllerApi {
        val mockCompanyDataControllerApi = mock(CompanyDataControllerApi::class.java)
        `when`(
            mockCompanyDataControllerApi.getCompanyById(
                companyId = any(),
            ),
        ).thenReturn(mockStoredCompany)
        return mockCompanyDataControllerApi
    }

    @BeforeEach
    fun setup() {
        mockMetadataControllerApi = setupMockMetaDataControllerApi()
        mockSfdrDataControllerApi = setupMockSfdrDataControllerApi()
        mockCompanyDataControllerApi = setupMockCompanyDataControllerApi()

        csvDataExporter =
            CsvExporter(
                metaDataControllerApi = mockMetadataControllerApi,
                sfdrDataControllerApi = mockSfdrDataControllerApi,
                companyDataControllerApi = mockCompanyDataControllerApi,
            )
    }

    @Test
    fun `check that the transformation rules cover all possible leaf nodes`() {
        val data = convertDataToJson(mockCompanyAssociatedSfdrDataWithNoNullFields)
        val transformationRules = readTransformationConfig("./transformationRules/SfdrSqlServer.config")
        assertDoesNotThrow { TransformationUtils.checkConsistencyOfDataAndTransformationRules(data, transformationRules) }
    }

    @Test
    fun `check that the sfdr export runs as expected`() {
        assertDoesNotThrow { csvDataExporter.exportSfdrData(outputDirectory) }
        verify(mockMetadataControllerApi, times(1))
            .getListOfDataMetaInfo(
                dataType = DataTypeEnum.sfdr,
            )
        verify(mockSfdrDataControllerApi, times(1))
            .getCompanyAssociatedSfdrDataByDimensions(
                reportingPeriod = any(),
                companyId = any(),
            )
        verify(mockCompanyDataControllerApi, times(1))
            .getCompanyById(
                companyId = any(),
            )
    }

    @Test
    fun `check that the csv exporter handles a socket timeout and terminates after MAX RETRIES tries`() {
        `when`(
            mockCompanyDataControllerApi.getCompanyById(
                companyId = any(),
            ),
        ).thenThrow(SocketTimeoutException())
        csvDataExporter.exportSfdrData(outputDirectory)
        verify(mockCompanyDataControllerApi, times(MAX_RETRIES))
            .getCompanyById(any())
    }

    @Test
    fun `check that the csv exporter handles a server exception and terminates after MAX RETRIES tries`() {
        `when`(
            mockCompanyDataControllerApi.getCompanyById(
                companyId = any(),
            ),
        ).thenThrow(ServerException())
        csvDataExporter.exportSfdrData(outputDirectory)
        verify(mockCompanyDataControllerApi, times(MAX_RETRIES))
            .getCompanyById(any())
    }

    @Test
    fun `check that the csv exporter handles a client exception and terminates after MAX RETRIES tries`() {
        `when`(
            mockCompanyDataControllerApi.getCompanyById(
                companyId = any(),
            ),
        ).thenThrow(
            ClientException(
                statusCode = CsvExporter.UNAUTHORIZED_CODE,
            ),
        )

        csvDataExporter.exportSfdrData(outputDirectory)
        verify(mockCompanyDataControllerApi, times(MAX_RETRIES))
            .getCompanyById(any())
    }

    @Test
    fun `check that running the sfdr export produces two new csv files`() {
        val directory = File(outputDirectory)
        if (directory.exists()) {
            directory.deleteRecursively()
        }

        csvDataExporter.exportSfdrData(outputDirectory)

        val filesInDirectory = File(outputDirectory).listFiles()
        assertTrue((filesInDirectory!!.size) == 2, "There should be exactly two new csv-files.")
    }
}
