package org.dataland.e2etests.tests.frameworks

import org.dataland.communitymanager.openApiClient.api.RequestControllerApi
import org.dataland.communitymanager.openApiClient.model.AccessStatus
import org.dataland.communitymanager.openApiClient.model.CompanyRole
import org.dataland.datalandbackend.openApiClient.api.VsmeDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataVsmeData
import org.dataland.datalandbackend.openApiClient.model.VsmeData
import org.dataland.datalandbackendutils.utils.sha256
import org.dataland.e2etests.BASE_PATH_TO_COMMUNITY_MANAGER
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_BACKEND
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.ExceptionUtils.assertAccessDeniedWrapper
import org.dataland.e2etests.utils.VsmeTestUtils
import org.dataland.e2etests.utils.testDataProvivders.FrameworkTestDataProvider
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.http.HttpStatus
import java.io.File
import java.math.BigDecimal
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class Vsme {

    private val apiAccessor = ApiAccessor()

    private val vsmeDataControllerApi = VsmeDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)

    private val requestControllerApi = RequestControllerApi(BASE_PATH_TO_COMMUNITY_MANAGER)

    private val testVsmeData = FrameworkTestDataProvider(VsmeData::class.java).getTData(1).first()

    private val dataAdminUserId = UUID.fromString(TechnicalUser.Admin.technicalUserId)

    private val vsmeTestUtils = VsmeTestUtils()

    private lateinit var dummyFileAlpha: File
    private lateinit var hashAlpha: String
    private val fileNameAlpha = "Report-Alpha"

    private lateinit var dummyFileBeta: File
    private lateinit var hashBeta: String

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

    @BeforeEach
    fun postCompanyAndSetCompanyOwnership() {
        companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        apiAccessor.companyRolesControllerApi.assignCompanyRole(
            CompanyRole.CompanyOwner,
            UUID.fromString(companyId),
            UUID.fromString(TechnicalUser.Uploader.technicalUserId),
        )
    }

    @AfterAll
    fun deleteDummyFiles() {
        assertTrue(dummyFileAlpha.delete())
        assertTrue(dummyFileBeta.delete())
    }

    @Test
    fun `post VSME data check its meta info persistence and that data is only accessible for company role holders `() {
        val vsmeData = vsmeTestUtils.setReferencedReports(testVsmeData, FileInfos(hashAlpha, fileNameAlpha))
        val companyAssociatedVsmeData = CompanyAssociatedDataVsmeData(companyId, "2022", vsmeData)
        val dataMetaInfoInResponse =
            vsmeTestUtils.postVsmeDataset(companyAssociatedVsmeData, listOf(dummyFileAlpha), TechnicalUser.Uploader)
        val persistedDataMetaInfo = executeDataRetrievalWithRetries(
            apiAccessor.metaDataControllerApi::getDataMetaInfo, dataMetaInfoInResponse.dataId,
        )
        assertEquals(persistedDataMetaInfo, dataMetaInfoInResponse)

        val dataId = persistedDataMetaInfo!!.dataId
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        assertAccessDeniedWrapper { vsmeDataControllerApi.getCompanyAssociatedVsmeData(dataId) }
        assertAccessDeniedWrapper { vsmeDataControllerApi.getPrivateDocument(dataId, hashAlpha) }

        for (role in CompanyRole.values()) {
            apiAccessor.companyRolesControllerApi.assignCompanyRole(
                role,
                companyId = UUID.fromString(companyId),
                userId = dataAdminUserId,
            )
            assertDoesNotThrow { vsmeDataControllerApi.getCompanyAssociatedVsmeData(persistedDataMetaInfo.dataId) }
            assertDoesNotThrow { vsmeDataControllerApi.getPrivateDocument(dataId, hashAlpha) }
            apiAccessor.companyRolesControllerApi.removeCompanyRole(
                role,
                companyId = UUID.fromString(companyId),
                userId = dataAdminUserId,
            )
        }
    }

    @Test
    fun `assert that only company owner and company data uploader can post VSME data`() {
        val companyAssociatedVsmeData = CompanyAssociatedDataVsmeData(companyId, "2021", testVsmeData)
        val rolesThatShouldBeAllowedToPost = listOf(CompanyRole.CompanyOwner, CompanyRole.DataUploader)

        for (role in CompanyRole.values()) {
            apiAccessor.companyRolesControllerApi.assignCompanyRole(
                role, companyId = UUID.fromString(companyId), userId = dataAdminUserId,
            )

            if (rolesThatShouldBeAllowedToPost.contains(role)) {
                assertDoesNotThrow {
                    vsmeTestUtils.postVsmeDataset(
                        companyAssociatedVsmeData, listOf(dummyFileAlpha),
                        TechnicalUser.Admin,
                    )
                }
            } else {
                assertAccessDeniedWrapper {
                    vsmeTestUtils.postVsmeDataset(
                        companyAssociatedVsmeData, listOf(dummyFileAlpha),
                        TechnicalUser.Admin,
                    )
                }
            }

            apiAccessor.companyRolesControllerApi.removeCompanyRole(
                role, companyId = UUID.fromString(companyId), userId = dataAdminUserId,
            )
        }
    }

    @Test
    fun `post VSME data with documents and check if data and documents match the uploaded data and documents`() {
        val vsmeData = vsmeTestUtils.setReferencedReports(testVsmeData, FileInfos(hashAlpha, fileNameAlpha))
        val companyAssociatedDataVsmeData = CompanyAssociatedDataVsmeData(companyId, "2023", vsmeData)
        val dataMetaInfoInResponse = vsmeTestUtils.postVsmeDataset(
            companyAssociatedDataVsmeData,
            listOf(dummyFileAlpha, dummyFileBeta),
            TechnicalUser.Uploader,
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
        var vsmeData = vsmeTestUtils.setReferencedReports(testVsmeData, null)
        val companyAssociatedVsmeDataAlpha =
            generateVsmeDataWithSetNumberOfEmployeesInHeadCount(companyId, "2022", vsmeData, BigDecimal(1))
        val dataIdAlpha = vsmeTestUtils.postVsmeDataset(companyAssociatedVsmeDataAlpha, user = TechnicalUser.Uploader)
            .dataId

        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        val retrievedCompanyAssociatedVsmeDataAlpha = executeDataRetrievalWithRetries(
            vsmeDataControllerApi::getCompanyAssociatedVsmeData, dataIdAlpha,
        )
        assertEquals(
            BigDecimal(1),
            retrievedCompanyAssociatedVsmeDataAlpha?.data?.basic?.workforceGeneralCharacteristics
                ?.numberOfEmployeesInHeadcount,
        )

        vsmeData = vsmeTestUtils.setReferencedReports(testVsmeData, FileInfos(hashAlpha, fileNameAlpha))
        val companyAssociatedVsmeDataBeta =
            generateVsmeDataWithSetNumberOfEmployeesInHeadCount(companyId, "2022", vsmeData, BigDecimal(2))

        val dataIdBeta =
            vsmeTestUtils.postVsmeDataset(
                companyAssociatedVsmeDataBeta, listOf(dummyFileAlpha, dummyFileBeta), TechnicalUser.Uploader,
            )
                .dataId
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
        val vsmeData = vsmeTestUtils.setReferencedReports(testVsmeData, FileInfos(hashAlpha, fileNameAlpha))
        val companyAssociatedVsmeData = CompanyAssociatedDataVsmeData(companyId, "2022", vsmeData)
        val dataId =
            vsmeTestUtils.postVsmeDataset(
                companyAssociatedVsmeData, listOf(dummyFileAlpha, dummyFileAlpha), TechnicalUser.Uploader,
            ).dataId

        val persistedDataMetaInfo = executeDataRetrievalWithRetries(
            apiAccessor.metaDataControllerApi::getDataMetaInfo, dataId,
        )

        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        val downloadedFile = vsmeDataControllerApi.getPrivateDocument(persistedDataMetaInfo!!.dataId, hashAlpha)
        assertEquals(hashAlpha, downloadedFile.readBytes().sha256())
    }

    @Test
    fun `post a VSME dataset and verify that a normal user with access status granted can retrieve the data`() {
        val vsmeData = vsmeTestUtils.setReferencedReports(testVsmeData, FileInfos(hashAlpha, fileNameAlpha))
        val companyAssociatedDataVsmeData = CompanyAssociatedDataVsmeData(companyId, "2022", vsmeData)
        val dataId =
            vsmeTestUtils.postVsmeDataset(
                companyAssociatedDataVsmeData, listOf(dummyFileAlpha, dummyFileAlpha), TechnicalUser.Uploader,
            ).dataId
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.PremiumUser)
        val requestId = createSingleDataVsmeRequest()
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        requestControllerApi.patchDataRequest(
            dataRequestId = UUID.fromString(requestId), accessStatus = AccessStatus.Granted,
        )
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.PremiumUser)
        val retrievedCompanyAssociatedVsmeData = executeDataRetrievalWithRetries(
            vsmeDataControllerApi::getCompanyAssociatedVsmeData, dataId,
        )

        assertEquals(companyAssociatedDataVsmeData, retrievedCompanyAssociatedVsmeData)
        val downloadedFile = vsmeDataControllerApi.getPrivateDocument(dataId, hashAlpha)

        assertEquals(hashAlpha, downloadedFile.readBytes().sha256())

        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        requestControllerApi.patchDataRequest(
            dataRequestId = UUID.fromString(requestId), accessStatus = AccessStatus.Revoked,
        )

        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.PremiumUser)
        assertAccessDeniedWrapper { vsmeDataControllerApi.getCompanyAssociatedVsmeData(dataId) }
    }

    private fun createSingleDataVsmeRequest(): String? {
        val vsmeDataRequest = vsmeTestUtils.setSingleDataVsmeRequest(companyId, setOf("2022"))

        requestControllerApi.postSingleDataRequest(vsmeDataRequest)
        Thread.sleep(1000)
        return requestControllerApi.getDataRequestsForRequestingUser().maxByOrNull { it.creationTimestamp }
            ?.dataRequestId
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
