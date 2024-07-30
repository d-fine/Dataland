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
import org.dataland.e2etests.utils.FrameworkTestDataProvider
import org.dataland.e2etests.utils.VsmeUtils
import org.dataland.e2etests.utils.communityManager.assertAccessDeniedResponseBodyInCommunityManagerClientException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AccessRequestTest {

    val apiAccessor = ApiAccessor()
    val vsmeUtils = VsmeUtils()
    private val requestControllerApi = RequestControllerApi(BASE_PATH_TO_COMMUNITY_MANAGER)
    val jwtHelper = JwtAuthenticationHelper()

    private val testVsmeData = FrameworkTestDataProvider(VsmeData::class.java).getTData(1).first()
    private lateinit var dummyFileAlpha: File
    private val fileNameAlpha = "Report-Alpha"
    private lateinit var hashAlpha: String

    private val timeSleep: Long = 3000
    lateinit var companyId: String

    @Test
    fun `premium User makes private request, has access to active dataset`() {
        companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        createVSMEDataAndPostAsAdminCompanyOwner(companyId)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.PremiumUser)

        val singleDataRequest = vsmeUtils.setSingleDataVSMERequest(companyId, setOf("2022"))
        requestControllerApi.postSingleDataRequest(singleDataRequest)
        Thread.sleep(timeSleep)

        val dataRequestReader = requestControllerApi.getDataRequestsForRequestingUser()

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        requestControllerApi.patchDataRequest(
            UUID.fromString(dataRequestReader[0].dataRequestId),
            accessStatus = AccessStatus.Granted,
        )

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.PremiumUser)

        Thread.sleep(timeSleep)
        assertEquals(AccessStatus.Granted, requestControllerApi.getDataRequestsForRequestingUser()[0].accessStatus)
        dummyFileAlpha.delete()
    }

    @Test
    fun `premium User makes private request, has no access to active dataset, no matching dataset`() {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.PremiumUser)

        val singleDataRequest = vsmeUtils.setSingleDataVSMERequest(
            apiAccessor.uploadOneCompanyWithRandomIdentifier()
                .actualStoredCompany.companyId,
            setOf("2022"),
        )

        requestControllerApi.postSingleDataRequest(singleDataRequest)
        Thread.sleep(timeSleep)
        val recentReaderDataRequest = requestControllerApi.getDataRequestsForRequestingUser().maxByOrNull {
            it.creationTimestamp
        }
        assertEquals(AccessStatus.Pending, recentReaderDataRequest?.accessStatus)
        assertEquals(RequestStatus.Open, recentReaderDataRequest?.requestStatus)
    }

    @Test
    fun `premium User makes private request, has no access to active dataset, matching dataset exists`() {
        companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        createVSMEDataAndPostAsAdminCompanyOwner(companyId)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.PremiumUser)

        val singleDataRequest = vsmeUtils.setSingleDataVSMERequest(companyId, setOf("2022"))
        requestControllerApi.postSingleDataRequest(singleDataRequest)
        Thread.sleep(timeSleep)
        val recentReaderDataRequest = requestControllerApi.getDataRequestsForRequestingUser().maxByOrNull {
            it.creationTimestamp
        }

        assertEquals(AccessStatus.Pending, recentReaderDataRequest?.accessStatus)
        assertEquals(RequestStatus.Answered, recentReaderDataRequest?.requestStatus)

        dummyFileAlpha.delete()
    }

    @Test
    fun `Comapny owner gets private request`() {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.PremiumUser)
        companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId

        val singleDataRequest = vsmeUtils.setSingleDataVSMERequest(companyId, setOf("2022"))

        requestControllerApi.postSingleDataRequest(singleDataRequest)
        Thread.sleep(timeSleep)
        var recentReaderDataRequest = requestControllerApi.getDataRequestsForRequestingUser().maxByOrNull {
            it.creationTimestamp
        }

        assertEquals(AccessStatus.Pending, recentReaderDataRequest?.accessStatus)
        assertEquals(RequestStatus.Open, recentReaderDataRequest?.requestStatus)

        createVSMEDataAndPostAsAdminCompanyOwner(companyId)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.PremiumUser)
        Thread.sleep(timeSleep)
        recentReaderDataRequest = requestControllerApi.getDataRequestsForRequestingUser().maxByOrNull {
            it.creationTimestamp
        }

        assertEquals(AccessStatus.Pending, recentReaderDataRequest?.accessStatus)
        assertEquals(RequestStatus.Answered, recentReaderDataRequest?.requestStatus)

        dummyFileAlpha.delete()
    }

    @Test
    fun `Company owner gets new access request, declines access`() {
        companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        createVSMEDataAndPostAsAdminCompanyOwner(companyId)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.PremiumUser)

        val singleDataRequest = vsmeUtils.setSingleDataVSMERequest(companyId, setOf("2022"))
        requestControllerApi.postSingleDataRequest(singleDataRequest)
        Thread.sleep(timeSleep)
        var recentReaderDataRequest = requestControllerApi.getDataRequestsForRequestingUser().maxByOrNull {
            it.creationTimestamp
        }

        assertEquals(AccessStatus.Pending, recentReaderDataRequest?.accessStatus)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        requestControllerApi.patchDataRequest(
            UUID.fromString(recentReaderDataRequest?.dataRequestId),
            accessStatus = AccessStatus.Declined,
        )

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.PremiumUser)
        Thread.sleep(timeSleep)
        recentReaderDataRequest = requestControllerApi.getDataRequestsForRequestingUser().maxByOrNull {
            it.creationTimestamp
        }

        assertEquals(AccessStatus.Declined, recentReaderDataRequest?.accessStatus)
        dummyFileAlpha.delete()
    }

    @Test
    fun `assures that user without proper rights are not able to patch the accessStatus of their own requests`() {
        companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        TechnicalUser.entries.forEach { technicalUser ->
            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(technicalUser)
            val singleDataRequest = vsmeUtils.setSingleDataVSMERequest(
                companyId = companyId,
                reportingPeriods = setOf("2022"),
            )
            requestControllerApi.postSingleDataRequest(singleDataRequest = singleDataRequest)
            Thread.sleep(timeSleep)
            val recentReaderDataRequest = requestControllerApi.getDataRequestsForRequestingUser().maxByOrNull {
                    request ->
                request.creationTimestamp
            }
            if (technicalUser == TechnicalUser.Admin) {
                val responseBody = requestControllerApi.patchDataRequest(
                    dataRequestId = UUID.fromString(recentReaderDataRequest?.dataRequestId),
                    accessStatus = AccessStatus.Declined,
                )
                assertEquals(AccessStatus.Declined, responseBody.accessStatus)
            } else {
                val responseException = assertThrows<ClientException> {
                    requestControllerApi.patchDataRequest(
                        dataRequestId = UUID.fromString(recentReaderDataRequest?.dataRequestId),
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
        val vsmeData = vsmeUtils.setReferencedReports(testVsmeData, FileInfos(hashAlpha, fileNameAlpha))
        val companyAssociatedVsmeData = CompanyAssociatedDataVsmeData(companyId, "2022", vsmeData)
        vsmeUtils.postVsmeDataset(companyAssociatedVsmeData, listOf(dummyFileAlpha), TechnicalUser.Admin)
    }
}
