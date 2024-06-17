package org.dataland.e2etests.tests.frameworks

import org.dataland.datalandbackend.openApiClient.api.SmeDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataSmeData
import org.dataland.datalandbackend.openApiClient.model.CompanyReport
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.SmeData
import org.dataland.datalandbackend.openApiClient.model.YesNoNa
import org.dataland.datalandbackendutils.utils.sha256
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_BACKEND
import org.dataland.e2etests.UPLOADER_USER_ID
import org.dataland.e2etests.UPLOADER_USER_NAME
import org.dataland.e2etests.UPLOADER_USER_PASSWORD
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.customApiControllers.CustomSmeDataControllerApi
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.FrameworkTestDataProvider
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import java.io.File
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class Sme {

    private val apiAccessor = ApiAccessor()

    val keycloakTokenUploader = apiAccessor.jwtHelper.requestToken(UPLOADER_USER_NAME, UPLOADER_USER_PASSWORD)
    val customSmeDataControllerApi = CustomSmeDataControllerApi(keycloakTokenUploader)

    val smeDataControllerApi = SmeDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)

    val testSmeData = FrameworkTestDataProvider(SmeData::class.java).getTData(1).first()

    lateinit var dummyFileAlpha: File
    lateinit var hashAlpha: String
    val fileNameAlpha = "Report-Alpha"

    lateinit var dummyFileBeta: File
    lateinit var hashBeta: String

    lateinit var companyId: String

    @BeforeAll
    fun prepareDummyFiles() {
        val threeMegabytes = 3 * 1000 * 1000
        val tenMegabytes = 10 * 1000 * 1000

        dummyFileAlpha = File("dummyFileAlpha.txt")
        dummyFileAlpha.writeBytes(ByteArray(threeMegabytes))
        hashAlpha = dummyFileAlpha.readBytes().sha256()

        dummyFileBeta = File("dummyFileBeta.txt")
        dummyFileBeta.writeBytes(ByteArray(tenMegabytes))
        hashBeta = dummyFileBeta.readBytes().sha256()
    }

    @BeforeAll
    fun postCompanyAndSetDataOwnership() {
        companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        apiAccessor.companyDataControllerApi.postDataOwner(
            UUID.fromString(companyId),
            UUID.fromString(UPLOADER_USER_ID),
        )
    }

    @AfterAll
    fun deleteDummyFiles() {
        assertTrue(dummyFileAlpha.delete())
        assertTrue(dummyFileBeta.delete())
    }

    @Test
    fun `post SME data and check its meta info persistence and that data is not even accessible to Dataland admins `() {
        val smeData = setReferencedReports(testSmeData, FileInfos(hashAlpha, fileNameAlpha))
        val companyAssociatedSmeData = CompanyAssociatedDataSmeData(companyId, "2022", smeData)
        val dataMetaInfoInResponse = postSmeDataset(companyAssociatedSmeData, listOf(dummyFileAlpha))
        val persistedDataMetaInfo = executeDataRetrievalWithRetries(
            apiAccessor.metaDataControllerApi::getDataMetaInfo, dataMetaInfoInResponse.dataId,
        )
        assertEquals(persistedDataMetaInfo, dataMetaInfoInResponse)

        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val exceptionForJson = assertThrows<ClientException> {
            smeDataControllerApi.getCompanyAssociatedSmeData(persistedDataMetaInfo!!.dataId)
        }
        assertTrue(exceptionForJson.message!!.contains("Client error : 403"))

        val exceptionForBlob = assertThrows<ClientException> {
            smeDataControllerApi.getPrivateDocument(persistedDataMetaInfo!!.dataId, hashAlpha)
        }
        assertTrue(exceptionForBlob.message!!.contains("Client error : 403"))
    }

    @Test
    fun `post SME data with documents and check if data and documents can be retrieved by the data owner`() {
        val smeData = setReferencedReports(testSmeData, FileInfos(hashAlpha, fileNameAlpha))
        val companyAssociatedDataSmeData = CompanyAssociatedDataSmeData(companyId, "2023", smeData)
        val dataMetaInfoInResponse = postSmeDataset(companyAssociatedDataSmeData, listOf(dummyFileAlpha, dummyFileBeta))

        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        val retrievedCompanyAssociatedSmeData = executeDataRetrievalWithRetries(
            smeDataControllerApi::getCompanyAssociatedSmeData, dataMetaInfoInResponse.dataId,
        )
        assertEquals(companyAssociatedDataSmeData, retrievedCompanyAssociatedSmeData)

        val downloadedAlpha = smeDataControllerApi.getPrivateDocument(dataMetaInfoInResponse.dataId, hashAlpha)
        assertEquals(hashAlpha, downloadedAlpha.readBytes().sha256())

        val downloadedBeta = smeDataControllerApi.getPrivateDocument(dataMetaInfoInResponse.dataId, hashBeta)
        assertEquals(hashBeta, downloadedBeta.readBytes().sha256())
    }

    @Test
    fun `post two SME datasets for the same reporting period and company and assert correct handling`() {
        var smeData = setReferencedReports(testSmeData, null)
        val companyAssociatedSmeDataAlpha =
            generateSmeDataWithSetNumberOfEmployeesInHeadCount(companyId, "2022", smeData, BigDecimal(1))
        val dataIdAlpha = postSmeDataset(companyAssociatedSmeDataAlpha).dataId

        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        val retrievedCompanyAssociatedSmeDataAlpha = executeDataRetrievalWithRetries(
            smeDataControllerApi::getCompanyAssociatedSmeData, dataIdAlpha,
        )
        assertEquals(
            BigDecimal(1),
            retrievedCompanyAssociatedSmeDataAlpha?.data?.basic?.workforceGeneralCharacteristics
                ?.numberOfEmployeesInHeadcount,
        )

        smeData = setReferencedReports(testSmeData, FileInfos(hashAlpha, fileNameAlpha))
        val companyAssociatedSmeDataBeta =
            generateSmeDataWithSetNumberOfEmployeesInHeadCount(companyId, "2022", smeData, BigDecimal(2))

        val dataIdBeta = postSmeDataset(companyAssociatedSmeDataBeta, listOf(dummyFileAlpha, dummyFileBeta)).dataId
        val retrievedCompanyAssociatedSmeDataBeta = executeDataRetrievalWithRetries(
            smeDataControllerApi::getCompanyAssociatedSmeData, dataIdBeta,
        )
        assertEquals(
            BigDecimal(2),
            retrievedCompanyAssociatedSmeDataBeta?.data?.basic?.workforceGeneralCharacteristics
                ?.numberOfEmployeesInHeadcount,
        )

        val persistedDataMetaInfoAlpha = executeDataRetrievalWithRetries(
            apiAccessor.metaDataControllerApi::getDataMetaInfo, dataIdAlpha,
        )
        assertEquals(false, persistedDataMetaInfoAlpha?.currentlyActive)

        val persistedDataMetaInfoBeta = executeDataRetrievalWithRetries(
            apiAccessor.metaDataControllerApi::getDataMetaInfo, dataIdBeta,
        )
        assertEquals(true, persistedDataMetaInfoBeta?.currentlyActive)
    }

    @Test
    fun `post an SME dataset with duplicate file and assert that the downloaded file is unique and correct`() {
        val smeData = setReferencedReports(testSmeData, FileInfos(hashAlpha, fileNameAlpha))
        val companyAssociatedSmeData = CompanyAssociatedDataSmeData(companyId, "2022", smeData)
        val dataId = postSmeDataset(companyAssociatedSmeData, listOf(dummyFileAlpha, dummyFileAlpha)).dataId

        val persistedDataMetaInfo = executeDataRetrievalWithRetries(
            apiAccessor.metaDataControllerApi::getDataMetaInfo, dataId,
        )

        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        val downloadedFile = smeDataControllerApi.getPrivateDocument(persistedDataMetaInfo!!.dataId, hashAlpha)
        assertEquals(hashAlpha, downloadedFile.readBytes().sha256())
    }

    private fun setReferencedReports(dataset: SmeData, fileInfoToSetAsReport: FileInfos?): SmeData {
        val newReferencedReports = fileInfoToSetAsReport?.let {
            mapOf(
                it.fileName to CompanyReport(
                    fileReference = it.fileReference,
                    fileName = it.fileName,
                    isGroupLevel = YesNoNa.Yes,
                    reportDate = LocalDate.now(),
                    currency = "EUR",
                ),
            )
        }
        return dataset.copy(
            basic = dataset.basic?.copy(
                basisForPreparation = dataset.basic?.basisForPreparation?.copy(
                    referencedReports = newReferencedReports,
                ),
            ),
        )
    }

    private fun postSmeDataset(
        companyAssociatedDataSmeData: CompanyAssociatedDataSmeData,
        documents: List<File> = listOf(),
    ): DataMetaInformation {
        return customSmeDataControllerApi.postCompanyAssociatedDataSmeData(
            companyAssociatedDataSmeData,
            documents,
        )
    }

    private fun <T>executeDataRetrievalWithRetries(action: (dataId: String) -> T, dataId: String): T? {
        val maxAttempts = 20
        var attempt = 1

        while (attempt <= maxAttempts) {
            Thread.sleep(500)
            try {
                return action(dataId)
            } catch (e: ClientException) {
                if (e.statusCode != HttpStatus.NOT_FOUND.value() || attempt == maxAttempts) {
                    // If it's not a client error, rethrow the exception
                    throw e
                }
                attempt++
            }
        }
        return null
    }

    private fun generateSmeDataWithSetNumberOfEmployeesInHeadCount(
        companyId: String,
        reportingPeriod: String,
        smeData: SmeData,
        numberOfEmployeesInHeadCount: BigDecimal,
    ): CompanyAssociatedDataSmeData {
        return CompanyAssociatedDataSmeData(
            companyId,
            reportingPeriod,
            smeData.copy(
                basic = smeData.basic?.let { basic ->
                    basic.copy(
                        workforceGeneralCharacteristics = basic.workforceGeneralCharacteristics?.copy(
                            numberOfEmployeesInHeadcount = numberOfEmployeesInHeadCount,
                        ),
                    )
                },
            ),
        )
    }

    class FileInfos(
        val fileReference: String,
        val fileName: String,
    )
}
