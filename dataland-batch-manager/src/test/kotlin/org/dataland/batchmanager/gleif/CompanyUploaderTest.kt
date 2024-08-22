package org.dataland.batchmanager.gleif

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientError
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.infrastructure.ServerException
import org.dataland.datalandbackend.openApiClient.model.CompanyId
import org.dataland.datalandbackend.openApiClient.model.CompanyInformationPatch
import org.dataland.datalandbackend.openApiClient.model.IdentifierType
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.datalandbatchmanager.model.ExternalCompanyInformation
import org.dataland.datalandbatchmanager.model.GleifCompanyCombinedInformation
import org.dataland.datalandbatchmanager.model.GleifCompanyInformation
import org.dataland.datalandbatchmanager.model.NorthDataCompanyInformation
import org.dataland.datalandbatchmanager.service.CompanyUploader
import org.dataland.datalandbatchmanager.service.CompanyUploader.Companion.UNAUTHORIZED_CODE
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.context.annotation.ComponentScan
import org.springframework.http.HttpStatus
import java.net.SocketTimeoutException
import java.util.stream.Stream

@ComponentScan(basePackages = ["org.dataland"])
class CompanyUploaderTest {
    private lateinit var mockCompanyDataControllerApi: CompanyDataControllerApi
    private lateinit var companyUploader: CompanyUploader
    private lateinit var mockStoredCompany: StoredCompany

    @BeforeEach
    fun setup() {
        mockCompanyDataControllerApi = mock(CompanyDataControllerApi::class.java)
        companyUploader = CompanyUploader(mockCompanyDataControllerApi, jacksonObjectMapper())
        mockStoredCompany = mock()
    }

    @Test
    fun `check that the upload of LEI ISIN mappings makes the intended calls`() {
        val deltaMap = mutableMapOf<String, Set<String>>()
        deltaMap["1000"] = setOf("1111", "1112", "1113")

        `when`(mockCompanyDataControllerApi.getCompanyIdByIdentifier(IdentifierType.Lei, "1000"))
            .thenReturn(CompanyId("testCompanyId"))

        companyUploader.updateIsins(deltaMap)

        val compPatch = CompanyInformationPatch(
            companyName = null,
            companyContactDetails = null,
            companyAlternativeNames = null,
            companyLegalForm = null,
            headquarters = null,
            headquartersPostalCode = null,
            sector = null,
            sectorCodeWz = null,
            identifiers = mapOf("Isin" to listOf("1111", "1112", "1113")),
            countryCode = null,
            website = null,
            isTeaserCompany = null,
        )

        verify(mockCompanyDataControllerApi, times(1)).getCompanyIdByIdentifier(IdentifierType.Lei, "1000")
        verify(mockCompanyDataControllerApi, times(1)).patchCompanyById("testCompanyId", compPatch)
    }

    @Test
    fun `check that the relationship update makes the intended calls`() {
        val finalParentMapping = mutableMapOf<String, String>()
        val mockLei = "abcd"
        val mockCompanyID = "testCompanyId"
        val mockParentLei = "defg"
        finalParentMapping[mockLei] = mockParentLei

        `when`(mockCompanyDataControllerApi.getCompanyIdByIdentifier(IdentifierType.Lei, mockLei))
            .thenReturn(CompanyId(mockCompanyID))

        companyUploader.updateRelationships(finalParentMapping)

        val compPatch = CompanyInformationPatch(parentCompanyLei = mockParentLei)

        verify(mockCompanyDataControllerApi, times(1)).getCompanyIdByIdentifier(IdentifierType.Lei, mockLei)
        verify(mockCompanyDataControllerApi, times(1)).patchCompanyById(mockCompanyID, compPatch)
    }

    @Test
    fun `test if any ClientException except status not found leads to a retry`() {
        val finalParentMapping = mutableMapOf<String, String>()
        val mockLei = "abcd"
        finalParentMapping[mockLei] = "defg"

        `when`(mockCompanyDataControllerApi.getCompanyIdByIdentifier(IdentifierType.Lei, mockLei))
            .thenThrow(ClientException(statusCode = HttpStatus.NOT_IMPLEMENTED.value()))

        companyUploader.updateRelationships(finalParentMapping)

        verify(mockCompanyDataControllerApi, times(CompanyUploader.MAX_RETRIES))
            .getCompanyIdByIdentifier(IdentifierType.Lei, mockLei)
    }

    @Test
    fun `test if ClientException http not found does not lead to multiple retries`() {
        val finalParentMapping = mutableMapOf<String, String>()
        val mockLei = "abcd"
        finalParentMapping[mockLei] = "defg"

        `when`(mockCompanyDataControllerApi.getCompanyIdByIdentifier(IdentifierType.Lei, mockLei))
            .thenThrow(ClientException(statusCode = HttpStatus.NOT_FOUND.value()))

        companyUploader.updateRelationships(finalParentMapping)

        verify(mockCompanyDataControllerApi, times(1))
            .getCompanyIdByIdentifier(IdentifierType.Lei, mockLei)
    }

    @Test
    fun `check that the upload requests are succesfully sent on the first try if the environment is ideal`() {
        companyUploader.uploadOrPatchSingleCompany(dummyGleifCompanyInformation1)
        companyUploader.uploadOrPatchSingleCompany(dummyGleifCompanyInformation2)

        verify(mockCompanyDataControllerApi, times(1)).postCompany(dummyGleifCompanyInformation1.toCompanyPost())
        verify(mockCompanyDataControllerApi, times(1)).postCompany(dummyGleifCompanyInformation2.toCompanyPost())
    }

    @Test
    fun `check that the upload handles a socket timeout and terminates after MAX RETRIES tries`() {
        `when`(
            mockCompanyDataControllerApi
                .postCompany(dummyGleifCompanyInformation1.toCompanyPost()),
        ).thenThrow(SocketTimeoutException())
        companyUploader.uploadOrPatchSingleCompany(dummyGleifCompanyInformation1)
        verify(mockCompanyDataControllerApi, times(CompanyUploader.MAX_RETRIES))
            .postCompany(dummyGleifCompanyInformation1.toCompanyPost())
    }

    @Test
    fun `check that the upload handles a server exception and terminates after MAX RETRIES tries`() {
        `when`(
            mockCompanyDataControllerApi
                .postCompany(dummyGleifCompanyInformation1.toCompanyPost()),
        ).thenThrow(ServerException())
        companyUploader.uploadOrPatchSingleCompany(dummyGleifCompanyInformation1)
        verify(mockCompanyDataControllerApi, times(CompanyUploader.MAX_RETRIES))
            .postCompany(dummyGleifCompanyInformation1.toCompanyPost())
    }

    @Test
    fun `check that the upload handles a client exception and terminates after MAX RETRIES tries`() {
        `when`(mockCompanyDataControllerApi.postCompany(dummyGleifCompanyInformation1.toCompanyPost())).thenThrow(
            ClientException(
                statusCode = UNAUTHORIZED_CODE,
            ),
        )
        companyUploader.uploadOrPatchSingleCompany(dummyGleifCompanyInformation1)
        verify(mockCompanyDataControllerApi, times(CompanyUploader.MAX_RETRIES)).postCompany(
            dummyGleifCompanyInformation1.toCompanyPost(),
        )
    }

    private fun readAndPrepareBadRequestClientException(resourceFileName: String): ClientException {
        val exceptionBodyContents = javaClass.getResourceAsStream(resourceFileName)!!.readAllBytes()
        val exceptionBodyString = String(exceptionBodyContents)
        return ClientException(
            statusCode = HttpStatus.BAD_REQUEST.value(),
            response = ClientError<Any>(
                body = exceptionBodyString,
            ),
        )
    }

    @ParameterizedTest
    @MethodSource("provideInputForDuplicateIdentifiers")
    fun `check that the upload handles a bad request exception and switches to patching on duplicate identifiers`(
        responseFilePath: String,
        numberOfPatchInvocations: Int,
        dummyCompanyInformation: ExternalCompanyInformation,
        expectedPatch: CompanyInformationPatch,
    ) {
        `when`(mockCompanyDataControllerApi.postCompany(dummyCompanyInformation.toCompanyPost())).thenThrow(
            readAndPrepareBadRequestClientException(responseFilePath),
        )
        companyUploader.uploadOrPatchSingleCompany(dummyCompanyInformation)
        verify(mockCompanyDataControllerApi, times(numberOfPatchInvocations))
            .patchCompanyById("violating-company-id", expectedPatch)
    }

    companion object {
        private val dummyGleifCompanyInformation1 = GleifCompanyCombinedInformation(
            GleifCompanyInformation(
                companyName = "CompanyName1",
                countryCode = "CompanyCountry",
                headquarters = "CompanyCity",
                headquartersPostalCode = "CompanyPostalCode",
                lei = "DummyLei1",
            ),
        )

        private val dummyGleifCompanyInformation2 = GleifCompanyCombinedInformation(
            GleifCompanyInformation(
                companyName = "CompanyName2",
                countryCode = "CompanyCountry",
                headquarters = "CompanyCity",
                headquartersPostalCode = "CompanyPostalCode",
                lei = "DummyLei2",
            ),
        )

        private val dummyNorthDataCompanyInformation3 = NorthDataCompanyInformation(
            companyName = "CompanyName3",
            countryCode = "CompanyCountry",
            headquarters = "CompanyCity",
            headquartersPostalCode = "CompanyPostalCode",
            lei = "dummy-lei1234",
            vatId = "",
            registerId = "Dummy HRB 12356",
            sector = "",
            status = "active",
            street = "Teststraße",
        )

        @JvmStatic
        fun provideInputForDuplicateIdentifiers(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    "/sampleResponseLeiIdentifierAlreadyExists.json",
                    1, dummyGleifCompanyInformation1,
                    dummyGleifCompanyInformation1.toCompanyPatch(),
                ),
                Arguments.of(
                    "/sampleResponseLeiIdentifierAlreadyExists.json",
                    1, dummyNorthDataCompanyInformation3,
                    dummyNorthDataCompanyInformation3.toCompanyPatch(),
                ),
                Arguments.of(
                    "/sampleResponseCompanyRegistrationNumberIdentifierAlreadyExists.json",
                    1, dummyNorthDataCompanyInformation3,
                    dummyNorthDataCompanyInformation3.toCompanyPatch(setOf("CompanyRegistrationNumber")),
                ),
                Arguments.of(
                    "/sampleResponseMultipleIdentifierAlreadyExists.json",
                    0, dummyNorthDataCompanyInformation3,
                    dummyNorthDataCompanyInformation3.toCompanyPatch(),
                ),
                Arguments.of(
                    "/sampleResponseMultipleIdentifierAlreadyExistsSameCompany.json",
                    1, dummyNorthDataCompanyInformation3,
                    dummyNorthDataCompanyInformation3.toCompanyPatch(),
                ),
            )
        }
    }
}
