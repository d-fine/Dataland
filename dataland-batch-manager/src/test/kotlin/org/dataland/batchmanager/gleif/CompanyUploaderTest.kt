package org.dataland.batchmanager.gleif

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientError
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.infrastructure.ServerException
import org.dataland.datalandbackend.openApiClient.model.*
import org.dataland.datalandbatchmanager.model.GleifCompanyInformation
import org.dataland.datalandbatchmanager.service.CompanyUploader
import org.dataland.datalandbatchmanager.service.CompanyUploader.Companion.UNAUTHORIZED_CODE
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.context.annotation.ComponentScan
import org.springframework.http.HttpStatus
import java.net.SocketTimeoutException

@ComponentScan(basePackages = ["org.dataland"])
class CompanyUploaderTest {
    private lateinit var mockCompanyDataControllerApi: CompanyDataControllerApi
    private lateinit var companyUploader: CompanyUploader
    private lateinit var mockStoredCompany: StoredCompany

    private val dummyCompanyInformation1 = GleifCompanyInformation(
        companyName = "CompanyName1",
        countryCode = "CompanyCountry",
        headquarters = "CompanyCity",
        headquartersPostalCode = "CompanyPostalCode",
        lei = "DummyLei1",
    )

    private val dummyCompanyInformation2 = GleifCompanyInformation(
        companyName = "CompanyName2",
        countryCode = "CompanyCountry",
        headquarters = "CompanyCity",
        headquartersPostalCode = "CompanyPostalCode",
        lei = "DummyLei2",
    )

    @BeforeEach
    fun setup() {
        mockCompanyDataControllerApi = mock(CompanyDataControllerApi::class.java)
        companyUploader = CompanyUploader(mockCompanyDataControllerApi, jacksonObjectMapper())
        mockStoredCompany = mock()
    }

    @Test
    fun `check that the upload of LEI-ISIN mappings makes the intended calls`() {
        val deltaMap = mutableMapOf<String, Set<String>>()
        deltaMap["1000"] = setOf("1111", "1112", "1113")
        // deltaMap["3000"] = setOf("3333")

        // `when`(mockCompanyDataControllerApi.getCompanyIdByIdentifier(IdentifierType.lei, "1000")).thenReturn("testCompanyId" as CompanyId)

        companyUploader.updateIsins(deltaMap)

        val compIdentifiers = mapOf(
            "Isin" to listOf("1111", "1112", "1113"),
        )
        val compPatch = CompanyInformationPatch(
            companyName = null, companyAlternativeNames = null, companyLegalForm = null,
            headquarters = null, headquartersPostalCode = null, sector = null, compIdentifiers, countryCode = null,
            website = null, teaserCompany = null, isTeaserCompany = null,
        )

        verify(mockCompanyDataControllerApi, times(1)).getCompanyIdByIdentifier(IdentifierType.lei, "1000")
        // verify(mockCompanyDataControllerApi, times(1)).getCompaniesBySearchString("3000")
        verify(mockCompanyDataControllerApi, times(1)).patchCompanyById("testCompanyId", compPatch)
    }

    @Test
    fun `check that the upload requests are succesfully sent on the first try if the environment is ideal`() {
        companyUploader.uploadOrPatchSingleCompany(dummyCompanyInformation1)
        companyUploader.uploadOrPatchSingleCompany(dummyCompanyInformation2)

        verify(mockCompanyDataControllerApi, times(1)).postCompany(dummyCompanyInformation1.toCompanyPost())
        verify(mockCompanyDataControllerApi, times(1)).postCompany(dummyCompanyInformation2.toCompanyPost())
    }

    @Test
    fun `check that the upload handles a socket timeout and terminates after two retries`() {
        `when`(
            mockCompanyDataControllerApi
                .postCompany(dummyCompanyInformation1.toCompanyPost()),
        ).thenThrow(SocketTimeoutException())
        companyUploader.uploadOrPatchSingleCompany(dummyCompanyInformation1)
        verify(mockCompanyDataControllerApi, times(3))
            .postCompany(dummyCompanyInformation1.toCompanyPost())
    }

    @Test
    fun `check that the upload handles a server exception and terminates after two retries`() {
        `when`(
            mockCompanyDataControllerApi
                .postCompany(dummyCompanyInformation1.toCompanyPost()),
        ).thenThrow(ServerException())
        companyUploader.uploadOrPatchSingleCompany(dummyCompanyInformation1)
        verify(mockCompanyDataControllerApi, times(3))
            .postCompany(dummyCompanyInformation1.toCompanyPost())
    }

    @Test
    fun `check that the upload handles a client exception and terminates after two retries`() {
        `when`(mockCompanyDataControllerApi.postCompany(dummyCompanyInformation1.toCompanyPost())).thenThrow(
            ClientException(
                statusCode = UNAUTHORIZED_CODE,
            ),
        )
        companyUploader.uploadOrPatchSingleCompany(dummyCompanyInformation1)
        verify(mockCompanyDataControllerApi, times(3)).postCompany(dummyCompanyInformation1.toCompanyPost())
    }

    @Test
    fun `check that the upload handles a bad request exception and switches to patching on duplicate identifiers`() {
        val exceptionBodyContents = javaClass.getResourceAsStream("/sampleResponseIdentifierAlreadyExists.json")
            .readAllBytes()
        val exceptionBodyString = String(exceptionBodyContents)

        `when`(mockCompanyDataControllerApi.postCompany(dummyCompanyInformation1.toCompanyPost())).thenThrow(
            ClientException(
                statusCode = HttpStatus.BAD_REQUEST.value(),
                response = ClientError<Any>(
                    body = exceptionBodyString,
                ),
            ),
        )
        companyUploader.uploadOrPatchSingleCompany(dummyCompanyInformation1)
        verify(mockCompanyDataControllerApi, times(1))
            .patchCompanyById("violating-company-id", dummyCompanyInformation1.toCompanyPatch())
    }
}
