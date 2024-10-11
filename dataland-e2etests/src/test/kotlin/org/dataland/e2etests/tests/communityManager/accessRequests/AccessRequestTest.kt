package org.dataland.e2etests.tests.communityManager.accessRequests

import org.dataland.communitymanager.openApiClient.api.RequestControllerApi
import org.dataland.communitymanager.openApiClient.infrastructure.ClientException
import org.dataland.communitymanager.openApiClient.model.AccessStatus
import org.dataland.communitymanager.openApiClient.model.CompanyRole
import org.dataland.communitymanager.openApiClient.model.RequestStatus
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataVsmeData
import org.dataland.datalandbackend.openApiClient.model.VsmeData
import org.dataland.datalandbackendutils.utils.sha256
import org.dataland.e2etests.BASE_PATH_TO_COMMUNITY_MANAGER
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.tests.frameworks.Vsme.FileInfos
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.VsmeTestUtils
import org.dataland.e2etests.utils.communityManager.assertAccessDeniedResponseBodyInCommunityManagerClientException
import org.dataland.e2etests.utils.communityManager.getNewlyStoredRequestsAfterTimestamp
import org.dataland.e2etests.utils.communityManager.retrieveTimeAndWaitOneMillisecond
import org.dataland.e2etests.utils.testDataProvivders.FrameworkTestDataProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AccessRequestTest {
    val apiAccessor = ApiAccessor()
    val vsmeTestUtils = VsmeTestUtils()
    private val requestControllerApi = RequestControllerApi(BASE_PATH_TO_COMMUNITY_MANAGER)
    val jwtHelper = JwtAuthenticationHelper()

    private val testVsmeData = FrameworkTestDataProvider(VsmeData::class.java).getTData(1).first()
    private lateinit var dummyFileAlpha: File
    private val fileNameAlpha = "Report-Alpha"
    private lateinit var hashAlpha: String

    lateinit var companyId: String

    @Test
    fun `premium user makes private request and has access to active dataset`() {
        companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        createVSMEDataAndPostAsAdminCompanyOwner(companyId)

        val timestampBeforeSingleRequest =
            postVSMERequestWithTimestampForTechnicalUser(
                TechnicalUser.PremiumUser, companyId, "2022",
            )

        val dataRequestId =
            UUID.fromString(
                getNewlyStoredRequestsAfterTimestamp(timestampBeforeSingleRequest)[0].dataRequestId,
            )

        val timestampBeforeSingleRequestSecond = retrieveTimeAndWaitOneMillisecond()
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        requestControllerApi.patchDataRequest(
            dataRequestId,
            accessStatus = AccessStatus.Granted,
        )
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.PremiumUser)

        val newlyStoredRequestsSecond =
            requestControllerApi.getDataRequestsForRequestingUser().filter { storedDataRequest ->
                storedDataRequest.lastModifiedDate > timestampBeforeSingleRequestSecond
            }
        assertEquals(AccessStatus.Granted, newlyStoredRequestsSecond[0].accessStatus)
        assertTrue(dummyFileAlpha.delete())
    }

    @Test
    fun `premium user makes private request and has no access to active dataset and no matching dataset`() {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.PremiumUser)

        val singleDataRequest =
            vsmeTestUtils.setSingleDataVsmeRequest(
                apiAccessor
                    .uploadOneCompanyWithRandomIdentifier()
                    .actualStoredCompany.companyId,
                setOf("2022"),
            )
        val timestampBeforeSingleRequest = retrieveTimeAndWaitOneMillisecond()
        requestControllerApi.postSingleDataRequest(singleDataRequest)

        val newlyStoredRequests = getNewlyStoredRequestsAfterTimestamp(timestampBeforeSingleRequest)
        assertEquals(AccessStatus.Pending, newlyStoredRequests[0].accessStatus)
        assertEquals(RequestStatus.Open, newlyStoredRequests[0].requestStatus)
    }

    @Test
    fun `premium user makes private request and has no access to active dataset and matching dataset exists`() {
        companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        createVSMEDataAndPostAsAdminCompanyOwner(companyId)

        val timestampBeforeSingleRequest =
            postVSMERequestWithTimestampForTechnicalUser(
                TechnicalUser.PremiumUser, companyId, "2022",
            )

        val newlyStoredRequests = getNewlyStoredRequestsAfterTimestamp(timestampBeforeSingleRequest)

        assertEquals(AccessStatus.Pending, newlyStoredRequests[0].accessStatus)
        assertEquals(RequestStatus.Answered, newlyStoredRequests[0].requestStatus)

        assertTrue(dummyFileAlpha.delete())
    }

    @Test
    fun `company owner gets private request`() {
        companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId

        val timestampBeforeSingleRequest =
            postVSMERequestWithTimestampForTechnicalUser(
                TechnicalUser.PremiumUser, companyId, "2022",
            )

        val newlyStoredRequests = getNewlyStoredRequestsAfterTimestamp(timestampBeforeSingleRequest)

        assertEquals(AccessStatus.Pending, newlyStoredRequests[0].accessStatus)
        assertEquals(RequestStatus.Open, newlyStoredRequests[0].requestStatus)
        val timestampBeforeSingleRequestSecond = retrieveTimeAndWaitOneMillisecond()
        createVSMEDataAndPostAsAdminCompanyOwner(companyId)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.PremiumUser)
        Thread.sleep(500)
        val newlyStoredRequestsSecond =
            requestControllerApi.getDataRequestsForRequestingUser().filter { storedDataRequest ->
                storedDataRequest.lastModifiedDate > timestampBeforeSingleRequestSecond
            }
        assertEquals(AccessStatus.Pending, newlyStoredRequestsSecond[0].accessStatus)
        assertEquals(RequestStatus.Answered, newlyStoredRequestsSecond[0].requestStatus)

        assertTrue(dummyFileAlpha.delete())
    }

    @Test
    fun `company owner gets new access request and declines access`() {
        companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        createVSMEDataAndPostAsAdminCompanyOwner(companyId)

        val timestampBeforeSingleRequest =
            postVSMERequestWithTimestampForTechnicalUser(
                TechnicalUser.PremiumUser, companyId, "2022",
            )

        val newlyStoredRequests = getNewlyStoredRequestsAfterTimestamp(timestampBeforeSingleRequest)
        assertEquals(AccessStatus.Pending, newlyStoredRequests[0].accessStatus)

        val timestampBeforeSingleRequestSecond = retrieveTimeAndWaitOneMillisecond()
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val dataRequestId =
            UUID.fromString(
                newlyStoredRequests[0].dataRequestId,
            )
        requestControllerApi.patchDataRequest(
            dataRequestId,
            accessStatus = AccessStatus.Declined,
        )

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.PremiumUser)
        val newlyStoredRequestsSecond =
            requestControllerApi.getDataRequestsForRequestingUser().filter { storedDataRequest ->
                storedDataRequest.lastModifiedDate > timestampBeforeSingleRequestSecond
            }

        assertEquals(AccessStatus.Declined, newlyStoredRequestsSecond[0].accessStatus)
        assertTrue(dummyFileAlpha.delete())
    }

    @Test
    fun `assures that user without proper rights are not able to patch the accessStatus of their own requests`() {
        companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        val listOfTechnicalUser = listOf(TechnicalUser.Admin, TechnicalUser.PremiumUser)
        listOfTechnicalUser.forEach { technicalUser ->
            val timestampBeforeSingleRequest =
                postVSMERequestWithTimestampForTechnicalUser(
                    technicalUser, companyId, "2022",
                )

            val newlyStoredRequests = getNewlyStoredRequestsAfterTimestamp(timestampBeforeSingleRequest)

            if (technicalUser == TechnicalUser.Admin) {
                val responseBody =
                    requestControllerApi.patchDataRequest(
                        dataRequestId = UUID.fromString(newlyStoredRequests[0].dataRequestId),
                        accessStatus = AccessStatus.Declined,
                    )
                assertEquals(AccessStatus.Declined, responseBody.accessStatus)
            } else {
                val responseException =
                    assertThrows<ClientException> {
                        requestControllerApi.patchDataRequest(
                            dataRequestId = UUID.fromString(newlyStoredRequests[0].dataRequestId),
                            accessStatus = AccessStatus.Declined,
                        )
                    }
                assertAccessDeniedResponseBodyInCommunityManagerClientException(responseException)
            }
        }
    }

    private fun createVSMEDataAndPostAsAdminCompanyOwner(companyId: String) {
        val threeMegabytes = 3 * 1000 * 1000
        dummyFileAlpha = File("dummyFileAlpha.txt")
        dummyFileAlpha.writeBytes(ByteArray(threeMegabytes))
        hashAlpha = dummyFileAlpha.readBytes().sha256()

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        apiAccessor.companyRolesControllerApi.assignCompanyRole(
            CompanyRole.CompanyOwner,
            UUID.fromString(companyId),
            UUID.fromString(TechnicalUser.Admin.technicalUserId),
        )
        val vsmeData = vsmeTestUtils.setReferencedReports(testVsmeData, FileInfos(hashAlpha, fileNameAlpha))
        val companyAssociatedVsmeData = CompanyAssociatedDataVsmeData(companyId, "2022", vsmeData)
        vsmeTestUtils.postVsmeDataset(companyAssociatedVsmeData, listOf(dummyFileAlpha), TechnicalUser.Admin)
    }

    private fun postVSMERequestWithTimestampForTechnicalUser(
        technicalUser: TechnicalUser,
        companyId: String,
        year: String,
    ): Long {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(technicalUser)
        val singleDataRequest = vsmeTestUtils.setSingleDataVsmeRequest(companyId, setOf(year))
        val timestampBeforeSingleRequest = retrieveTimeAndWaitOneMillisecond()
        requestControllerApi.postSingleDataRequest(singleDataRequest)
        return timestampBeforeSingleRequest
    }
}
