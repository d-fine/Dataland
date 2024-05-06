package org.dataland.e2etests.tests.frameworks

import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataSmeData
import org.dataland.datalandbackend.openApiClient.model.QaStatus
import org.dataland.datalandbackend.openApiClient.model.SmeData
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
import java.io.File
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class Sme {

    private val apiAccessor = ApiAccessor()

    val keycloakToken = apiAccessor.jwtHelper.requestToken(UPLOADER_USER_NAME, UPLOADER_USER_PASSWORD)
    val customSmeDataControllerApi = CustomSmeDataControllerApi(keycloakToken)
    val testDataProviderForSmeData = FrameworkTestDataProvider(SmeData::class.java)

    val testSmeData = testDataProviderForSmeData.getTData(1).first()

    val dummyFileAlpha = File("dummyFileAlpha.txt")
    val dummyFileBeta = File("dummyFileBeta.txt")

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

        Thread.sleep(2000) // Wait required to give the asynchronous EuroDaT storage process enough time to finish

        val persistedDataMetaInfo =
            apiAccessor.metaDataControllerApi.getDataMetaInfo(initialDataMetaInfo.dataId)

        assertEquals(QaStatus.Pending, initialDataMetaInfo.qaStatus)
        assertEquals(false, initialDataMetaInfo.currentlyActive)

        assertEquals(QaStatus.Accepted, persistedDataMetaInfo.qaStatus)
        assertEquals(true, persistedDataMetaInfo.currentlyActive)

        assertEquals(persistedDataMetaInfo.dataId, initialDataMetaInfo.dataId)
        assertEquals(persistedDataMetaInfo.dataType, initialDataMetaInfo.dataType)
        assertEquals(persistedDataMetaInfo.companyId, initialDataMetaInfo.companyId)
        assertEquals(persistedDataMetaInfo.reportingPeriod, initialDataMetaInfo.reportingPeriod)
        assertEquals(persistedDataMetaInfo.uploadTime, initialDataMetaInfo.uploadTime)
        assertEquals(persistedDataMetaInfo.uploaderUserId, initialDataMetaInfo.uploaderUserId)
    }
}
