package org.dataland.batchmanager.gleif

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.infrastructure.ServerException
import org.dataland.datalandbackend.openApiClient.model.CompanyIdentifier
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbatchmanager.service.CompanyUploader
import org.dataland.datalandbatchmanager.service.CompanyUploader.Companion.UNAUTHORIZED_CODE
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.context.annotation.ComponentScan
import java.net.SocketTimeoutException

@ComponentScan(basePackages = ["org.dataland"])
class CompanyUploaderTest {
    private lateinit var mockCompanyDataControllerApi: CompanyDataControllerApi
    private lateinit var companyUploader: CompanyUploader

    private val dummyCompanyInformation1 = CompanyInformation(
        companyName = "CompanyName1",
        companyAlternativeNames = null,
        companyLegalForm = null,
        countryCode = "CompanyCountry",
        headquarters = "CompanyCity",
        headquartersPostalCode = "CompanyPostalCode",
        sector = "dummy",
        website = null,
        identifiers = mapOf(
            CompanyIdentifier.IdentifierType.lei.value to listOf("DummyLei1"),
        ),
    )

    private val dummyCompanyInformation2 = CompanyInformation(
        companyName = "CompanyName2",
        companyAlternativeNames = null,
        companyLegalForm = null,
        countryCode = "CompanyCountry",
        headquarters = "CompanyCity",
        headquartersPostalCode = "CompanyPostalCode",
        sector = "dummy",
        website = null,
        identifiers = mapOf(
            CompanyIdentifier.IdentifierType.lei.value to listOf("DummyLei2"),
        ),
    )

    @BeforeEach
    fun setup() {
        mockCompanyDataControllerApi = mock(CompanyDataControllerApi::class.java)
        companyUploader = CompanyUploader(mockCompanyDataControllerApi)
    }

    @Test
    fun `check that the upload requests are succesfully sent on the first try if the environment is ideal`() {
        companyUploader.uploadSingleCompany(dummyCompanyInformation1)
        companyUploader.uploadSingleCompany(dummyCompanyInformation2)

        verify(mockCompanyDataControllerApi, times(1)).postCompany(dummyCompanyInformation1)
        verify(mockCompanyDataControllerApi, times(1)).postCompany(dummyCompanyInformation2)
    }

    @Test
    fun `check that the upload handles a socket timeout and terminates after two retries`() {
        `when`(mockCompanyDataControllerApi.postCompany(dummyCompanyInformation1)).thenThrow(SocketTimeoutException())
        companyUploader.uploadSingleCompany(dummyCompanyInformation1)
        verify(mockCompanyDataControllerApi, times(3)).postCompany(dummyCompanyInformation1)
    }

    @Test
    fun `check that the upload handles a server exception and terminates after two retries`() {
        `when`(mockCompanyDataControllerApi.postCompany(dummyCompanyInformation1)).thenThrow(ServerException())
        companyUploader.uploadSingleCompany(dummyCompanyInformation1)
        verify(mockCompanyDataControllerApi, times(3)).postCompany(dummyCompanyInformation1)
    }

    @Test
    fun `check that the upload handles a client exception due to not being authorized and terminates`() {
        `when`(mockCompanyDataControllerApi.postCompany(dummyCompanyInformation1)).thenThrow(
            ClientException(
                statusCode = UNAUTHORIZED_CODE,
            ),
        )
        companyUploader.uploadSingleCompany(dummyCompanyInformation1)
        verify(mockCompanyDataControllerApi, times(1)).postCompany(dummyCompanyInformation1)
    }
}
