package org.dataland.e2etests.tests.communityManager.accessRequests

import org.dataland.communitymanager.openApiClient.api.RequestControllerApi
import org.dataland.communitymanager.openApiClient.model.AccessStatus
import org.dataland.communitymanager.openApiClient.model.CompanyRole
import org.dataland.communitymanager.openApiClient.model.RequestStatus
import org.dataland.communitymanager.openApiClient.model.SingleDataRequest
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataVsmeData
import org.dataland.datalandbackend.openApiClient.model.CompanyReport
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.VsmeData
import org.dataland.datalandbackendutils.utils.sha256
import org.dataland.e2etests.BASE_PATH_TO_COMMUNITY_MANAGER
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.customApiControllers.CustomVsmeDataControllerApi
import org.dataland.e2etests.tests.frameworks.Vsme.FileInfos
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.FrameworkTestDataProvider
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File
import java.time.LocalDate
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AccessToActiveMatchingDatasetTest {

    val apiAccessor = ApiAccessor()
    private val requestControllerApi = RequestControllerApi(BASE_PATH_TO_COMMUNITY_MANAGER)
    val jwtHelper = JwtAuthenticationHelper()

    private val testVsmeData = FrameworkTestDataProvider(VsmeData::class.java).getTData(1).first()
    private lateinit var dummyFileAlpha: File
    private val fileNameAlpha = "Report-Alpha"
    private lateinit var hashAlpha: String

    private val timeSleep: Long = 3000
    lateinit var companyId: String

    @AfterAll
    fun deleteDummyFiles() {
        assertTrue(dummyFileAlpha.delete())
    }

    @Test
    fun privateFrameworkHasAccess() {
        // TODO perhaps put the upload vsme files structure into a before all
        companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        createVSMEDataAndPostAsAdminCompanyOwner(companyId)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)

        val singleDataRequest = setSingleDataVSMERequest(companyId)
        requestControllerApi.postSingleDataRequest(singleDataRequest)
        Thread.sleep(timeSleep)

        val dataRequestReader = requestControllerApi.getDataRequestsForRequestingUser()

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        requestControllerApi.patchDataRequest(
            UUID.fromString(dataRequestReader[0].dataRequestId),
            accessStatus = AccessStatus.Granted,
        )

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        assertEquals(AccessStatus.Granted, requestControllerApi.getDataRequestsForRequestingUser()[0].accessStatus)
    }

    @Test
    fun privateFrameworkHasNoAccessNoMatchingDataset() {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)

        val singleDataRequest = setSingleDataVSMERequest(
            apiAccessor.uploadOneCompanyWithRandomIdentifier()
                .actualStoredCompany.companyId,
        )

        requestControllerApi.postSingleDataRequest(singleDataRequest)
        Thread.sleep(timeSleep)
        // val recentReaderDataRequest = requestControllerApi.getDataRequestsForRequestingUser()[0]
        val recentReaderDataRequest = requestControllerApi.getDataRequestsForRequestingUser().maxByOrNull {
            it.creationTimestamp
        }
        // todo variable aus den beiden unteren Zeilen
        assertEquals(AccessStatus.Pending, recentReaderDataRequest?.accessStatus)
        assertEquals(RequestStatus.Open, recentReaderDataRequest?.requestStatus)
    }

    @Test
    fun privateFrameworkHasNoAccessHasMatchingDataset() {
        companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        createVSMEDataAndPostAsAdminCompanyOwner(companyId)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)

        val singleDataRequest = setSingleDataVSMERequest(companyId)
        requestControllerApi.postSingleDataRequest(singleDataRequest)
        // TODO Maybe find different solution to Thread.sleep
        Thread.sleep(timeSleep)
        val recentReaderDataRequest = requestControllerApi.getDataRequestsForRequestingUser().maxByOrNull {
            it.creationTimestamp
        } //

        assertEquals(AccessStatus.Pending, recentReaderDataRequest?.accessStatus)
        assertEquals(RequestStatus.Answered, recentReaderDataRequest?.requestStatus)
    }

    @Test
    fun privateFrameworkCompanyOwnerPrivateRequestAnswer() {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId

        val singleDataRequest = setSingleDataVSMERequest(companyId)

        requestControllerApi.postSingleDataRequest(singleDataRequest)
        Thread.sleep(timeSleep)
        // var recentReaderDataRequest = requestControllerApi.getDataRequestsForRequestingUser()[0]
        var recentReaderDataRequest = requestControllerApi.getDataRequestsForRequestingUser().maxByOrNull {
            it.creationTimestamp
        }

        assertEquals(AccessStatus.Pending, recentReaderDataRequest?.accessStatus)
        assertEquals(RequestStatus.Open, recentReaderDataRequest?.requestStatus)

        createVSMEDataAndPostAsAdminCompanyOwner(companyId)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        Thread.sleep(timeSleep)
        recentReaderDataRequest = requestControllerApi.getDataRequestsForRequestingUser().maxByOrNull {
            it.creationTimestamp
        }

        assertEquals(AccessStatus.Pending, recentReaderDataRequest?.accessStatus)
        assertEquals(RequestStatus.Answered, recentReaderDataRequest?.requestStatus)
    }

    @Test
    fun privateFrameworkCompanyOwnerManagesNewRequestDeclined() {
        companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        createVSMEDataAndPostAsAdminCompanyOwner(companyId)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)

        val singleDataRequest = setSingleDataVSMERequest(companyId)
        requestControllerApi.postSingleDataRequest(singleDataRequest)
        // TODO Maybe find different solution to Thread.sleep
        Thread.sleep(timeSleep)
        var recentReaderDataRequest = requestControllerApi.getDataRequestsForRequestingUser().maxByOrNull {
            it.creationTimestamp
        }

        assertEquals(AccessStatus.Pending, recentReaderDataRequest?.accessStatus)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        requestControllerApi.patchDataRequest(
            UUID.fromString(recentReaderDataRequest?.dataRequestId),
            accessStatus = AccessStatus.Declined,
        )
        Thread.sleep(timeSleep)

        recentReaderDataRequest = requestControllerApi.getDataRequestsForRequestingUser().maxByOrNull {
            it.creationTimestamp
        }

        assertEquals(AccessStatus.Declined, recentReaderDataRequest?.accessStatus)
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
        val vsmeData = setReferencedReports(testVsmeData, FileInfos(hashAlpha, fileNameAlpha))
        // TODO clean up code duplication with functions in vsme.kt
        val companyAssociatedVsmeData = CompanyAssociatedDataVsmeData(companyId, "2022", vsmeData)
        postVsmeDataset(companyAssociatedVsmeData, listOf(dummyFileAlpha), TechnicalUser.Admin)
    }

    private fun setSingleDataVSMERequest(companyId: String): SingleDataRequest {
        return SingleDataRequest(
            companyIdentifier = companyId,
            dataType = SingleDataRequest.DataType.vsme,
            reportingPeriods = setOf("2022"),
            contacts = setOf("someContact@example.com"),
            message = "This is a test. The current timestamp is ${System.currentTimeMillis()}",
        )
    }

    private fun setReferencedReports(dataset: VsmeData, fileInfoToSetAsReport: FileInfos?): VsmeData {
        val newReferencedReports = fileInfoToSetAsReport?.let {
            mapOf(
                it.fileName to CompanyReport(
                    fileReference = it.fileReference,
                    fileName = it.fileName,
                    publicationDate = LocalDate.now(),
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
        user: TechnicalUser,
    ): DataMetaInformation {
        val keycloakToken = apiAccessor.jwtHelper.obtainJwtForTechnicalUser(user)
        val customVsmeDataControllerApi = CustomVsmeDataControllerApi(keycloakToken)
        return customVsmeDataControllerApi.postCompanyAssociatedDataVsmeData(
            companyAssociatedDataVsmeData,
            documents,
        )
    }
}
