package org.dataland.e2etests.tests.frameworks

import org.dataland.datalandbackend.openApiClient.api.SmeDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataSmeData
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.SmeData
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
import org.springframework.http.HttpStatus
import java.io.File
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class Sme {

    private val apiAccessor = ApiAccessor()

    val keycloakTokenUploader = apiAccessor.jwtHelper.requestToken(UPLOADER_USER_NAME, UPLOADER_USER_PASSWORD)
    val customSmeDataControllerApi = CustomSmeDataControllerApi(keycloakTokenUploader)

    val smeDataControllerApi = SmeDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)

    val testSmeData = sortSmeNaturalHazardsCovered(
        FrameworkTestDataProvider(SmeData::class.java).getTData(1).first(),
    )

    val dummyFileAlpha = File("dummyFileAlpha.txt")
    val dummyFileBeta = File("dummyFileBeta.txt")
    lateinit var companyId: String

    @BeforeAll
    fun prepareDummyFiles() {
        val threeMegabytes = 3 * 1000 * 1000
        val tenMegabytes = 10 * 1000 * 1000

        dummyFileAlpha.writeBytes(ByteArray(threeMegabytes))
        dummyFileBeta.writeBytes(ByteArray(tenMegabytes))
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
    fun `post a company with SME data and check if it has been persisted successfully with correct data meta info`() {
        val companyAssociatedDataSmeData = CompanyAssociatedDataSmeData(companyId, "2022", testSmeData)
        val dataMetaInfoInResponse = postSmeDataset(companyAssociatedDataSmeData, listOf(dummyFileAlpha, dummyFileBeta))
        val persistedDataMetaInfo = executeDataRetrievalWithRetries(
            apiAccessor.metaDataControllerApi::getDataMetaInfo, dataMetaInfoInResponse.dataId,
        )
        assertEquals(persistedDataMetaInfo, dataMetaInfoInResponse)
    }

    @Test
    fun `post a company with SME data and check if it can be retrieved including the associated documents`() {
        val companyAssociatedDataSmeData = CompanyAssociatedDataSmeData(companyId, "2023", testSmeData)
        val expectedHashAlpha = dummyFileAlpha.readBytes().sha256()
        val expectedHashBeta = dummyFileBeta.readBytes().sha256()

        val dataMetaInfoInResponse = postSmeDataset(companyAssociatedDataSmeData, listOf(dummyFileAlpha, dummyFileBeta))

        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        val retrievedCompanyAssociatedSmeData = executeDataRetrievalWithRetries(
            smeDataControllerApi::getCompanyAssociatedSmeData, dataMetaInfoInResponse.dataId,
        )
        val downloadedAlpha = smeDataControllerApi.getPrivateDocument(dataMetaInfoInResponse.dataId, expectedHashAlpha)
        val downloadedBeta = smeDataControllerApi.getPrivateDocument(dataMetaInfoInResponse.dataId, expectedHashBeta)

        assertEquals(companyAssociatedDataSmeData, retrievedCompanyAssociatedSmeData)
        assertEquals(expectedHashAlpha, downloadedAlpha.readBytes().sha256())
        assertEquals(expectedHashBeta, downloadedBeta.readBytes().sha256())
    }

    @Test
    fun `post two SME datasets for the same reporting period and company and assert correct handling`() {
        val companyAssociatedSmeDataAlpha = CompanyAssociatedDataSmeData(
            companyId, "2022",
            setNumberOfEmployees(testSmeData, 1),
        )
        val dataIdAlpha = postSmeDataset(companyAssociatedSmeDataAlpha).dataId

        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        val retrievedCompanyAssociatedSmeDataAlpha = executeDataRetrievalWithRetries(
            smeDataControllerApi::getCompanyAssociatedSmeData, dataIdAlpha,
        )

        val companyAssociatedSmeDataBeta = CompanyAssociatedDataSmeData(
            companyId, "2022",
            setNumberOfEmployees(testSmeData, 2),
        )
        val dataIdBeta = postSmeDataset(companyAssociatedSmeDataBeta, listOf(dummyFileAlpha, dummyFileBeta)).dataId
        val retrievedCompanyAssociatedSmeDataBeta = executeDataRetrievalWithRetries(
            smeDataControllerApi::getCompanyAssociatedSmeData, dataIdBeta,
        )

        val persistedDataMetaInfoAlpha = executeDataRetrievalWithRetries(
            apiAccessor.metaDataControllerApi::getDataMetaInfo, dataIdAlpha,
        )
        assertEquals(false, persistedDataMetaInfoAlpha?.currentlyActive)
        assertEquals(1, retrievedCompanyAssociatedSmeDataAlpha?.data?.general?.basicInformation?.numberOfEmployees)

        val persistedDataMetaInfoBeta = executeDataRetrievalWithRetries(
            apiAccessor.metaDataControllerApi::getDataMetaInfo, dataIdBeta,
        )
        assertEquals(true, persistedDataMetaInfoBeta?.currentlyActive)
        assertEquals(2, retrievedCompanyAssociatedSmeDataBeta?.data?.general?.basicInformation?.numberOfEmployees)
    }

    @Test
    fun `post an SME dataset with duplicate files and assert that the downloaded file is unique and correct`() {
        val companyAssociatedSmeData = CompanyAssociatedDataSmeData(companyId, "2022", testSmeData)
        val dataId = postSmeDataset(companyAssociatedSmeData, listOf(dummyFileAlpha, dummyFileAlpha)).dataId

        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        val persistedDataMetaInfo = executeDataRetrievalWithRetries(
            apiAccessor.metaDataControllerApi::getDataMetaInfo, dataId,
        )

        val expectedHash = dummyFileAlpha.readBytes().sha256()
        val downloadedFile = smeDataControllerApi.getPrivateDocument(persistedDataMetaInfo!!.dataId, expectedHash)
        assertEquals(expectedHash, downloadedFile.readBytes().sha256())
    }

    // TODO test für keinen access für andere?  Not even Admin!

    private fun sortSmeNaturalHazardsCovered(dataset: SmeData): SmeData {
        return dataset.copy(
            insurances = dataset.insurances?.copy(
                naturalHazards = dataset.insurances?.naturalHazards?.copy(
                    naturalHazardsCovered = dataset.insurances?.naturalHazards?.naturalHazardsCovered?.sorted(),
                ),
            ),
        )
    }

    private fun setNumberOfEmployees(dataset: SmeData, numberOfEmployees: Int): SmeData {
        return dataset.copy(
            general = dataset.general.copy(
                basicInformation = dataset.general.basicInformation.copy(
                    numberOfEmployees = numberOfEmployees,
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
}
