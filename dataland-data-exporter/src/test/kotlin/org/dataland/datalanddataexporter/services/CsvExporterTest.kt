package org.dataland.datalanddataexporter.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.SfdrDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataSfdrData
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.QaStatus
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.datalanddataexporter.TestDataProvider
import org.dataland.datalanddataexporter.utils.TransformationUtils.ISIN_IDENTIFIER
import org.dataland.datalanddataexporter.utils.TransformationUtils.LEI_IDENTIFIER
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any

class CsvExporterTest {
    private lateinit var csvDataExporter: CsvExporter
    private lateinit var mockMetadataControllerApi: MetaDataControllerApi
    private lateinit var mockSfdrDataControllerApi: SfdrDataControllerApi
    private lateinit var mockCompanyDataControllerApi: CompanyDataControllerApi

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
            ),
        )

    private val mockCompanyAssociatedSfdrData =
        CompanyAssociatedDataSfdrData(
            companyId = "mockCompanyId",
            reportingPeriod = "2021",
            data = TestDataProvider.getMockSfdrData(),
        )

    private fun setupMockMetaDataControllerApi(): MetaDataControllerApi {
        val mockMetaDataControllerApi = mock(MetaDataControllerApi::class.java)
        `when`(
            mockMetaDataControllerApi.getListOfDataMetaInfo(
                dataType = any(),
                reportingPeriod = any(),
                companyId = any(),
                showOnlyActive = any(),
                uploaderUserIds = any(),
                qaStatus = any(),
            ),
        ).thenReturn(mockMetaData)
        return mockMetaDataControllerApi
    }

    private fun setupMockSfdrDataControllerApi(): SfdrDataControllerApi {
        val mockSfdrDataControllerApi = mock(SfdrDataControllerApi::class.java)
        `when`(
            mockSfdrDataControllerApi.getCompanyAssociatedSfdrData(
                dataId = any(),
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
    fun `check that running the sfdr export does not throw an error`() {
        assertDoesNotThrow { csvDataExporter.exportSfdrData(outputDirectory = "./src/test/resources/csv/output") }
    }
}
