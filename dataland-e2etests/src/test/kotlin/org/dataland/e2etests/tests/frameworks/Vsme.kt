package org.dataland.e2etests.tests.frameworks

import org.dataland.datalandbackend.openApiClient.api.VsmeDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataVsmeData
import org.dataland.datalandbackend.openApiClient.model.CompanyReport
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.VsmeData
import org.dataland.datalandbackend.openApiClient.model.YesNoNa
import org.dataland.datalandbackendutils.utils.sha256
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_BACKEND
import org.dataland.e2etests.UPLOADER_USER_ID
import org.dataland.e2etests.UPLOADER_USER_NAME
import org.dataland.e2etests.UPLOADER_USER_PASSWORD
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.customApiControllers.CustomVsmeDataControllerApi
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
class Vsme {

    private val apiAccessor = ApiAccessor()

    val keycloakTokenUploader = apiAccessor.jwtHelper.requestToken(UPLOADER_USER_NAME, UPLOADER_USER_PASSWORD)
    val customVsmeDataControllerApi = CustomVsmeDataControllerApi(keycloakTokenUploader)

    val vsmeDataControllerApi = VsmeDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)

    val testVsmeData = FrameworkTestDataProvider(VsmeData::class.java).getTData(1).first()

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
    fun `post VSME data check its meta info persistence and that data is not even accessible to Dataland admins `() {
        val vsmeData = setReferencedReports(testVsmeData, FileInfos(hashAlpha, fileNameAlpha))
        val companyAssociatedvsmeData = CompanyAssociatedDataVsmeData(companyId, "2022", vsmeData)
        val dataMetaInfoInResponse = postVsmeDataset(companyAssociatedvsmeData, listOf(dummyFileAlpha))
        val persistedDataMetaInfo = executeDataRetrievalWithRetries(
            apiAccessor.metaDataControllerApi::getDataMetaInfo, dataMetaInfoInResponse.dataId,
        )
        assertEquals(persistedDataMetaInfo, dataMetaInfoInResponse)

        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val exceptionForJson = assertThrows<ClientException> {
            vsmeDataControllerApi.getCompanyAssociatedVsmeData(persistedDataMetaInfo!!.dataId)
        }
        assertTrue(exceptionForJson.message!!.contains("Client error : 403"))

        val exceptionForBlob = assertThrows<ClientException> {
            vsmeDataControllerApi.getPrivateDocument(persistedDataMetaInfo!!.dataId, hashAlpha)
        }
        assertTrue(exceptionForBlob.message!!.contains("Client error : 403"))
    }

    @Test
    fun `post VSME data with documents and check if data and documents can be retrieved by the data owner`() {
        val vsmeData = setReferencedReports(testVsmeData, FileInfos(hashAlpha, fileNameAlpha))
        val companyAssociatedDataVsmeData = CompanyAssociatedDataVsmeData(companyId, "2023", vsmeData)
        val dataMetaInfoInResponse = postVsmeDataset(
            companyAssociatedDataVsmeData,
            listOf(dummyFileAlpha, dummyFileBeta),
        )

        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        val retrievedCompanyAssociatedVsmeData = executeDataRetrievalWithRetries(
            vsmeDataControllerApi::getCompanyAssociatedVsmeData, dataMetaInfoInResponse.dataId,
        )
        assertEquals(companyAssociatedDataVsmeData, retrievedCompanyAssociatedVsmeData)

        val downloadedAlpha = vsmeDataControllerApi.getPrivateDocument(dataMetaInfoInResponse.dataId, hashAlpha)
        assertEquals(hashAlpha, downloadedAlpha.readBytes().sha256())

        val downloadedBeta = vsmeDataControllerApi.getPrivateDocument(dataMetaInfoInResponse.dataId, hashBeta)
        assertEquals(hashBeta, downloadedBeta.readBytes().sha256())
    }

    @Test
    fun `post two VSME datasets for the same reporting period and company and assert correct handling`() {
        var vsmeData = setReferencedReports(testVsmeData, null)
        val companyAssociatedVsmeDataAlpha =
            generateVsmeDataWithSetNumberOfEmployeesInHeadCount(companyId, "2022", vsmeData, BigDecimal(1))
        val dataIdAlpha = postVsmeDataset(companyAssociatedVsmeDataAlpha).dataId

        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        val retrievedCompanyAssociatedVsmeDataAlpha = executeDataRetrievalWithRetries(
            vsmeDataControllerApi::getCompanyAssociatedVsmeData, dataIdAlpha,
        )
        assertEquals(
            BigDecimal(1),
            retrievedCompanyAssociatedVsmeDataAlpha?.data?.basic?.workforceGeneralCharacteristics
                ?.numberOfEmployeesInHeadcount,
        )

        vsmeData = setReferencedReports(testVsmeData, FileInfos(hashAlpha, fileNameAlpha))
        val companyAssociatedVsmeDataBeta =
            generateVsmeDataWithSetNumberOfEmployeesInHeadCount(companyId, "2022", vsmeData, BigDecimal(2))

        val dataIdBeta = postVsmeDataset(companyAssociatedVsmeDataBeta, listOf(dummyFileAlpha, dummyFileBeta)).dataId
        checkValidity(dataIdAlpha, dataIdBeta)
    }

    private fun checkValidity(dataIdAlpha: String, dataIdBeta: String) {
        val retrievedCompanyAssociatedVsmeDataBeta = executeDataRetrievalWithRetries(
            vsmeDataControllerApi::getCompanyAssociatedVsmeData, dataIdBeta,
        )
        assertEquals(
            BigDecimal(2),
            retrievedCompanyAssociatedVsmeDataBeta?.data?.basic?.workforceGeneralCharacteristics
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
    fun `post an VSME dataset with duplicate file and assert that the downloaded file is unique and correct`() {
        val vsmeData = setReferencedReports(testVsmeData, FileInfos(hashAlpha, fileNameAlpha))
        val companyAssociatedVsmeData = CompanyAssociatedDataVsmeData(companyId, "2022", vsmeData)
        val dataId = postVsmeDataset(companyAssociatedVsmeData, listOf(dummyFileAlpha, dummyFileAlpha)).dataId

        val persistedDataMetaInfo = executeDataRetrievalWithRetries(
            apiAccessor.metaDataControllerApi::getDataMetaInfo, dataId,
        )

        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        val downloadedFile = vsmeDataControllerApi.getPrivateDocument(persistedDataMetaInfo!!.dataId, hashAlpha)
        assertEquals(hashAlpha, downloadedFile.readBytes().sha256())
    }

    private fun setReferencedReports(dataset: VsmeData, fileInfoToSetAsReport: FileInfos?): VsmeData {
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

    private fun postVsmeDataset(
        companyAssociatedDataVsmeData: CompanyAssociatedDataVsmeData,
        documents: List<File> = listOf(),
    ): DataMetaInformation {
        return customVsmeDataControllerApi.postCompanyAssociatedDataVsmeData(
            companyAssociatedDataVsmeData,
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

    private fun generateVsmeDataWithSetNumberOfEmployeesInHeadCount(
        companyId: String,
        reportingPeriod: String,
        vsmeData: VsmeData,
        numberOfEmployeesInHeadCount: BigDecimal,
    ): CompanyAssociatedDataVsmeData {
        return CompanyAssociatedDataVsmeData(
            companyId,
            reportingPeriod,
            vsmeData.copy(
                basic = vsmeData.basic?.let { basic ->
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
