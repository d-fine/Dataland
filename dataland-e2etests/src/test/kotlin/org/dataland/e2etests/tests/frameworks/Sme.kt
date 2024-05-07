package org.dataland.e2etests.tests.frameworks

import org.dataland.datalandbackend.openApiClient.api.SmeDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataSmeData
import org.dataland.datalandbackend.openApiClient.model.QaStatus
import org.dataland.datalandbackend.openApiClient.model.SmeData
import org.dataland.datalandbackendutils.utils.sha256
import org.dataland.documentmanager.openApiClient.infrastructure.ClientException
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_BACKEND
import org.dataland.e2etests.UPLOADER_USER_ID
import org.dataland.e2etests.UPLOADER_USER_NAME
import org.dataland.e2etests.UPLOADER_USER_PASSWORD
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

    val keycloakToken = apiAccessor.jwtHelper.requestToken(UPLOADER_USER_NAME, UPLOADER_USER_PASSWORD)
    val customSmeDataControllerApi = CustomSmeDataControllerApi(keycloakToken)
    val smeDataControllerApi = SmeDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    val testDataProviderForSmeData = FrameworkTestDataProvider(SmeData::class.java)

    val testSmeData = testDataProviderForSmeData.getTData(1).first()

    val dummyFileAlpha = File("dummyFileAlpha.txt")
    val dummyFileBeta = File("dummyFileBeta.txt")
    private val pdfDocument = File("./public/test-report.pdf")

    @BeforeAll
    fun prepareDummyFiles() {
        val threeMegabytes = 3 * 1000 * 1000
        val tenMegabytes = 10 * 1000 * 1000

        dummyFileAlpha.writeBytes(ByteArray(threeMegabytes))
        dummyFileBeta.writeBytes(ByteArray(tenMegabytes))
    }

    @AfterAll
    fun deleteDummyFiles() {
        assertTrue(dummyFileAlpha.delete())
        assertTrue(dummyFileBeta.delete())
    }

    // TODO emanuel check if you can directly compare the data objects
    // TODO check for both tests if the wait time can be reduced again
    @Test
    fun `post a company with SME data including documents and check if it has been persisted successfully`() {
        val companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        apiAccessor.companyDataControllerApi.postDataOwner(
            UUID.fromString(companyId),
            UUID.fromString(UPLOADER_USER_ID),
        )
        val companyAssociatedDataSmeData = CompanyAssociatedDataSmeData(companyId, "2022", testSmeData)

        val initialDataMetaInfo = customSmeDataControllerApi.postCompanyAssociatedDataSmeData(
            companyAssociatedDataSmeData,
            listOf(dummyFileAlpha, dummyFileBeta),
        )

        Thread.sleep(6000) // Wait required to give the asynchronous EuroDaT storage process enough time to finish

        val persistedDataMetaInfo =
            apiAccessor.metaDataControllerApi.getDataMetaInfo(initialDataMetaInfo.dataId)

        assertEquals(QaStatus.Accepted, initialDataMetaInfo.qaStatus)
        assertEquals(true, initialDataMetaInfo.currentlyActive)

        assertEquals(QaStatus.Accepted, persistedDataMetaInfo.qaStatus)
        assertEquals(true, persistedDataMetaInfo.currentlyActive)

        assertEquals(persistedDataMetaInfo.dataId, initialDataMetaInfo.dataId)
        assertEquals(persistedDataMetaInfo.dataType, initialDataMetaInfo.dataType)
        assertEquals(persistedDataMetaInfo.companyId, initialDataMetaInfo.companyId)
        assertEquals(persistedDataMetaInfo.reportingPeriod, initialDataMetaInfo.reportingPeriod)
        assertEquals(persistedDataMetaInfo.uploadTime, initialDataMetaInfo.uploadTime)
        assertEquals(persistedDataMetaInfo.uploaderUserId, initialDataMetaInfo.uploaderUserId)
    }

    @Test
    fun `test that a dataset and a dummy pdf document can be uploaded and retrieved`() {
        val companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        apiAccessor.companyDataControllerApi.postDataOwner(
            UUID.fromString(companyId),
            UUID.fromString(UPLOADER_USER_ID),
        )

        val document = pdfDocument
        val companyAssociatedDataSmeData = CompanyAssociatedDataSmeData(companyId, "2022", testSmeData)
        val expectedHash = document.readBytes().sha256()
        val initialDataMetaInfo = customSmeDataControllerApi.postCompanyAssociatedDataSmeData(
            companyAssociatedDataSmeData,
            listOf(document),
        )
        Thread.sleep(5000) // Wait required to give the asynchronous EuroDaT storage process enough time to finish
        lateinit var downloadedFile: File
        lateinit var retrievedSmeData: CompanyAssociatedDataSmeData
        try {
            retrievedSmeData = smeDataControllerApi.getCompanyAssociatedSmeData(initialDataMetaInfo.dataId)
            downloadedFile = smeDataControllerApi.getPrivateDocument(initialDataMetaInfo.dataId, expectedHash)
        } catch (e: ClientException) {
            e.statusCode != HttpStatus.NOT_FOUND.value()
        }
        assertEquals(
            getSmeNaturalHazardsCoveredSorted(companyAssociatedDataSmeData.data),
            getSmeNaturalHazardsCoveredSorted(retrievedSmeData.data),
        )
        assertEquals(companyAssociatedDataSmeData.companyId, retrievedSmeData.companyId)
        assertEquals(companyAssociatedDataSmeData.reportingPeriod, retrievedSmeData.reportingPeriod)
        assertEquals(expectedHash, downloadedFile.readBytes().sha256())
    }
    private fun getSmeNaturalHazardsCoveredSorted(dataset: SmeData): SmeData {
        return dataset.copy(
            insurances = dataset.insurances?.copy(
                naturalHazards = dataset.insurances?.naturalHazards?.copy(
                    naturalHazardsCovered = dataset.insurances?.naturalHazards?.naturalHazardsCovered?.sorted(),
                ),
            ),
        )
    }
}
