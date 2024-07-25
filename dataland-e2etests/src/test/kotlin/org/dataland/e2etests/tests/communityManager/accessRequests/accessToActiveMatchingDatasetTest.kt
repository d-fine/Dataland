package org.dataland.e2etests.tests.communityManager.accessRequests

import org.dataland.communitymanager.openApiClient.api.RequestControllerApi
import org.dataland.communitymanager.openApiClient.infrastructure.ResponseType
import org.dataland.communitymanager.openApiClient.model.AccessStatus
import org.dataland.communitymanager.openApiClient.model.CompanyRole
import org.dataland.communitymanager.openApiClient.model.RequestStatus
import org.dataland.communitymanager.openApiClient.model.SingleDataRequest
import org.dataland.datalandbackend.openApiClient.api.VsmeDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataVsmeData
import org.dataland.datalandbackend.openApiClient.model.CompanyReport
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.VsmeData
import org.dataland.datalandbackendutils.utils.sha256
import org.dataland.e2etests.BASE_PATH_TO_COMMUNITY_MANAGER
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_BACKEND
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.customApiControllers.CustomVsmeDataControllerApi
import org.dataland.e2etests.tests.frameworks.Vsme.FileInfos
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.FrameworkTestDataProvider
import org.dataland.e2etests.utils.communityManager.getNewlyStoredRequestsAfterTimestamp
import org.dataland.e2etests.utils.communityManager.retrieveTimeAndWaitOneMillisecond
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File
import java.time.LocalDate
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class accessToActiveMatchingDatasetTest {

    val apiAccessor = ApiAccessor()
    private val requestControllerApi = RequestControllerApi(BASE_PATH_TO_COMMUNITY_MANAGER)
    val jwtHelper = JwtAuthenticationHelper()
    private val testDataEuTaxonomyNonFinancials = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
        .getTData(1).first()

    private val testCompanyInformation = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
        .getCompanyInformationWithoutIdentifiers(1).first()

    private val vsmeDataControllerApi = VsmeDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    private val testVsmeData = FrameworkTestDataProvider(VsmeData::class.java).getTData(1).first()
    private lateinit var dummyFileAlpha: File
    private val fileNameAlpha = "Report-Alpha"
    private lateinit var hashAlpha: String
    lateinit var companyId: String

    @Test
    fun privateFrameworkHasAccess() {
        //TODO perhaps put the upload vsme files structure into a before all
        val threeMegabytes = 3 * 1000 * 1000
        dummyFileAlpha = File("dummyFileAlpha.txt")
        dummyFileAlpha.writeBytes(ByteArray(threeMegabytes))
        hashAlpha = dummyFileAlpha.readBytes().sha256()

        companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        apiAccessor.companyRolesControllerApi.assignCompanyRole(
            CompanyRole.CompanyOwner,
            UUID.fromString(companyId),
            UUID.fromString(TechnicalUser.Admin.technicalUserId),
        )

        val vsmeData = setReferencedReports(testVsmeData, FileInfos(hashAlpha, fileNameAlpha))
        val companyAssociatedVsmeData = CompanyAssociatedDataVsmeData(companyId, "2022", vsmeData)
        postVsmeDataset(companyAssociatedVsmeData, listOf(dummyFileAlpha), TechnicalUser.Uploader)



        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)

        val singleDataRequest = SingleDataRequest(
            companyIdentifier = companyId,
            dataType = SingleDataRequest.DataType.vsme,
            reportingPeriods = setOf("2022"),
            contacts = setOf("someContact@example.com"),
            message = "This is a test. The current timestamp is ${System.currentTimeMillis()}",
        )

        requestControllerApi.postSingleDataRequest(singleDataRequest)
        val dataRequestReader = requestControllerApi.getDataRequestsForRequestingUser()

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)

        requestControllerApi.patchDataRequest(UUID.fromString(dataRequestReader[0].dataRequestId), accessStatus = AccessStatus.Granted)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)

        assertEquals(requestControllerApi.getDataRequestsForRequestingUser()[0].accessStatus, AccessStatus.Granted)

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



    @Test
    fun privateFrameworkHasNoAccessNoMatchingDataset() {

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)

        val stringThatMatchesThePermIdRegex = System.currentTimeMillis().toString()

        val singleDataRequest = SingleDataRequest(
            companyIdentifier = stringThatMatchesThePermIdRegex,
            dataType = SingleDataRequest.DataType.vsme,
            reportingPeriods = setOf("2022"),
            contacts = setOf("someContact@example.com"),
            message = "This is a test. The current timestamp is ${System.currentTimeMillis()}",
        )

        requestControllerApi.postSingleDataRequest(singleDataRequest)
        //todo variable aus den beiden unteren Zeilen
        assertEquals(requestControllerApi.getDataRequestsForRequestingUser()[0].accessStatus, AccessStatus.Pending)
        assertEquals(requestControllerApi.getDataRequestsForRequestingUser()[0].requestStatus, RequestStatus.Open)

    }


    @Test
    fun privateFrameworkHasNoAccessHasMatchingDataset() {

        // as admin upload correct data which will be requested

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)

        val stringThatMatchesThePermIdRegex = System.currentTimeMillis().toString()

        val singleDataRequest = SingleDataRequest(
            companyIdentifier = stringThatMatchesThePermIdRegex,
            dataType = SingleDataRequest.DataType.vsme,
            reportingPeriods = setOf("2022"),
            contacts = setOf("someContact@example.com"),
            message = "This is a test. The current timestamp is ${System.currentTimeMillis()}",
        )

        requestControllerApi.postSingleDataRequest(singleDataRequest)

        assertEquals(requestControllerApi.getDataRequestById(UUID.fromString(companyId)).accessStatus, AccessStatus.Pending)

        // check that there is no matching dataset and check if the request is being processed
        val checkRequestDoesntExist = requestControllerApi.getDataRequestById(UUID.fromString(singleDataRequest.companyIdentifier))
        assertEquals(ResponseType.Success, checkRequestDoesntExist)

    }



}