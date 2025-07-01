package org.dataland.batchmanager.gleif

import ch.qos.logback.classic.Logger
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.dataland.batchmanager.service.TestLogAppender
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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.ComponentScan
import org.springframework.http.HttpStatus
import java.net.SocketTimeoutException
import java.util.stream.Stream

@ComponentScan(basePackages = ["org.dataland"])
class CompanyUploaderTest {
    private lateinit var mockCompanyDataControllerApi: CompanyDataControllerApi
    private lateinit var companyUploader: CompanyUploader
    private lateinit var mockStoredCompany: StoredCompany
    lateinit var logAppender: TestLogAppender
    lateinit var logger: Logger

    @BeforeEach
    fun setup() {
        mockCompanyDataControllerApi = mock(CompanyDataControllerApi::class.java)
        companyUploader = CompanyUploader(mockCompanyDataControllerApi, jacksonObjectMapper())
        mockStoredCompany = mock()
    }

    @BeforeEach
    fun setUpLogAppender() {
        logger = LoggerFactory.getLogger(CompanyUploader::class.java) as Logger
        logAppender = TestLogAppender()
        logAppender.start()
        logger.addAppender(logAppender)
    }

    @AfterEach
    fun tearDownLogAppender() {
        logger.detachAppender(logAppender)
    }

    @Test
    fun `check that the upload of LEI ISIN mappings makes the intended calls`() {
        val deltaMap = mutableMapOf<String, Set<String>>()
        deltaMap["1000"] = setOf("1111", "1112", "1113")

        whenever(mockCompanyDataControllerApi.getCompanyIdByIdentifier(IdentifierType.Lei, "1000"))
            .thenReturn(CompanyId("testCompanyId"))

        companyUploader.updateIsins(deltaMap)

        val compPatch =
            CompanyInformationPatch(
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

        whenever(mockCompanyDataControllerApi.getCompanyIdByIdentifier(IdentifierType.Lei, mockLei))
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

        whenever(mockCompanyDataControllerApi.getCompanyIdByIdentifier(IdentifierType.Lei, mockLei))
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

        whenever(mockCompanyDataControllerApi.getCompanyIdByIdentifier(IdentifierType.Lei, mockLei))
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
        whenever(
            mockCompanyDataControllerApi
                .postCompany(dummyGleifCompanyInformation1.toCompanyPost()),
        ).thenThrow(SocketTimeoutException())
        companyUploader.uploadOrPatchSingleCompany(dummyGleifCompanyInformation1)
        verify(mockCompanyDataControllerApi, times(CompanyUploader.MAX_RETRIES))
            .postCompany(dummyGleifCompanyInformation1.toCompanyPost())
    }

    @Test
    fun `check that the upload handles a server exception and terminates after MAX RETRIES tries`() {
        whenever(
            mockCompanyDataControllerApi
                .postCompany(dummyGleifCompanyInformation1.toCompanyPost()),
        ).thenThrow(ServerException())
        companyUploader.uploadOrPatchSingleCompany(dummyGleifCompanyInformation1)
        verify(mockCompanyDataControllerApi, times(CompanyUploader.MAX_RETRIES))
            .postCompany(dummyGleifCompanyInformation1.toCompanyPost())
    }

    @Test
    fun `check that the upload handles a client exception and terminates after MAX RETRIES tries`() {
        whenever(mockCompanyDataControllerApi.postCompany(dummyGleifCompanyInformation1.toCompanyPost())).thenThrow(
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
            response =
                ClientError<Any>(
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
        whenever(mockCompanyDataControllerApi.postCompany(dummyCompanyInformation.toCompanyPost())).thenThrow(
            readAndPrepareBadRequestClientException(responseFilePath),
        )
        companyUploader.uploadOrPatchSingleCompany(dummyCompanyInformation)

        verify(mockCompanyDataControllerApi, times(numberOfPatchInvocations))
            .patchCompanyById("violating-company-id", expectedPatch)
    }

    companion object {
        private val dummyGleifCompanyInformation1 =
            GleifCompanyCombinedInformation(
                GleifCompanyInformation(
                    companyName = "CompanyName1",
                    countryCode = "CompanyCountry",
                    headquarters = "CompanyCity",
                    headquartersPostalCode = "CompanyPostalCode",
                    lei = "DummyLei1",
                ),
            )

        private val dummyGleifCompanyInformation2 =
            GleifCompanyCombinedInformation(
                GleifCompanyInformation(
                    companyName = "CompanyName2",
                    countryCode = "CompanyCountry",
                    headquarters = "CompanyCity",
                    headquartersPostalCode = "CompanyPostalCode",
                    lei = "DummyLei2",
                ),
            )

        private val dummyNorthDataCompanyInformation3 =
            NorthDataCompanyInformation(
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
        fun provideInputForDuplicateIdentifiers(): Stream<Arguments> =
            Stream.of(
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

    @Test
    fun `updateIsins logs on successful patch`() {
        val lei = "LEI123"
        val companyId = "company-id-123"
        val isins = setOf("ISIN1", "ISIN2")
        val mapping = mapOf(lei to isins)

        whenever(mockCompanyDataControllerApi.getCompanyIdByIdentifier(IdentifierType.Lei, lei))
            .thenReturn(CompanyId(companyId))

        companyUploader.updateIsins(mapping)

        val patchLog = logAppender.events.find { it.message.contains("Patching company with ID: $companyId and LEI: $lei") }
        val updateLog = logAppender.events.find { it.message.contains("Updating ISINs of company with ID: $companyId") }

        assertThat(patchLog).isNotNull
        assertThat(updateLog).isNotNull
    }

    @Test
    fun `updateIsins logs retry attempt from resilience4j if first patch fails`() {
        val lei = "LEI456"
        val companyId = "company-id-456"
        val isins = setOf("ISIN3", "ISIN4")
        val mapping = mapOf(lei to isins)
        val mockStoredCompany = mock(StoredCompany::class.java)

        whenever(mockCompanyDataControllerApi.getCompanyIdByIdentifier(IdentifierType.Lei, lei))
            .thenReturn(CompanyId(companyId))
        whenever(mockCompanyDataControllerApi.patchCompanyById(eq(companyId), any()))
            .thenThrow(ClientException())
            .thenReturn(mockStoredCompany)

        companyUploader.updateIsins(mapping)

        println("Captured logs:")
        logAppender.events.forEach {
            println("${it.level}: ${it.message}")
        }

        val retryAttemptLog =
            logAppender.events.find {
                it.level.levelStr == "WARN" &&
                    it.message.contains("Retry attempt #1 failed")
            }

        assertThat(retryAttemptLog).withFailMessage("Expected retry attempt log from Resilience4j").isNotNull
    }

    @Test
    fun `updateIsins logs error if retry also fails`() {
        val lei = "LEI789"
        val companyId = "company-id-789"
        val isins = setOf("ISIN5")
        val mapping = mapOf(lei to isins)
        val patch = CompanyInformationPatch(identifiers = mapOf("Isin" to isins.toList()))

        whenever(mockCompanyDataControllerApi.getCompanyIdByIdentifier(IdentifierType.Lei, lei))
            .thenReturn(CompanyId(companyId))
        whenever(mockCompanyDataControllerApi.patchCompanyById(companyId, patch))
            .thenThrow(ClientException("500"))

        companyUploader.updateIsins(mapping)

        val errorLog =
            logAppender.events.find {
                it.level.toString() == "ERROR" &&
                    it.message.contains("Retry failed due to") &&
                    it.message.contains(companyId)
            }

        assertThat(errorLog).isNotNull
    }

    @Test
    fun `updateIsins logs error if company not found`() {
        val lei = "LEI404"
        val isins = setOf("ISIN404")
        val mapping = mapOf(lei to isins)

        whenever(mockCompanyDataControllerApi.getCompanyIdByIdentifier(IdentifierType.Lei, lei))
            .thenThrow(ClientException(statusCode = HttpStatus.NOT_FOUND.value()))

        companyUploader.updateIsins(mapping)

        val notFoundLog =
            logAppender.events.find {
                it.level.toString() == "ERROR" &&
                    it.message.contains("Could not find company with LEI: $lei")
            }

        assertThat(notFoundLog).isNotNull
    }
}
