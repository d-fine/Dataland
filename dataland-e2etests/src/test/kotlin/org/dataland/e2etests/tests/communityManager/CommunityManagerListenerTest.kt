package org.dataland.e2etests.tests.communityManager

import org.awaitility.Awaitility
import org.dataland.communitymanager.openApiClient.infrastructure.ClientError
import org.dataland.communitymanager.openApiClient.infrastructure.ClientException
import org.dataland.communitymanager.openApiClient.model.AccessStatus
import org.dataland.communitymanager.openApiClient.model.DataRequestPatch
import org.dataland.communitymanager.openApiClient.model.ExtendedStoredDataRequest
import org.dataland.communitymanager.openApiClient.model.RequestStatus
import org.dataland.communitymanager.openApiClient.model.SingleDataRequest
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEutaxonomyNonFinancialsData
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataSfdrData
import org.dataland.datalandbackend.openApiClient.model.SfdrData
import org.dataland.datalandqaservice.openApiClient.model.QaStatus
import org.dataland.e2etests.auth.GlobalAuth
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.DocumentControllerApiAccessor
import org.dataland.e2etests.utils.communityManager.assertStatusForDataRequestId
import org.dataland.e2etests.utils.communityManager.checkThatAllReportingPeriodsAreTreatedAsExpected
import org.dataland.e2etests.utils.communityManager.getIdForUploadedCompanyWithIdentifiers
import org.dataland.e2etests.utils.communityManager.getNewlyStoredRequestsAfterTimestamp
import org.dataland.e2etests.utils.communityManager.getUsersStoredRequestWithLatestCreationTime
import org.dataland.e2etests.utils.communityManager.patchDataRequestAndAssertNewStatusAndLastModifiedUpdated
import org.dataland.e2etests.utils.communityManager.postStandardSingleDataRequest
import org.dataland.e2etests.utils.communityManager.retrieveTimeAndWaitOneMillisecond
import org.dataland.e2etests.utils.testDataProviders.FrameworkTestDataProvider
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import java.util.UUID
import java.util.concurrent.TimeUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CommunityManagerListenerTest {
    val apiAccessor = ApiAccessor()
    private val documentManagerAccessor = DocumentControllerApiAccessor()
    val jwtHelper = JwtAuthenticationHelper()
    private val requestControllerApi = apiAccessor.requestControllerApi
    private val dataController = apiAccessor.dataControllerApiForEuTaxonomyNonFinancials
    private lateinit var dummyCompanyAssociatedData: CompanyAssociatedDataEutaxonomyNonFinancialsData
    private val testDataEuTaxonomyNonFinancials =
        apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
            .getTData(1)
            .first()

    private val testCompanyInformation =
        apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
            .getCompanyInformationWithoutIdentifiers(1)
            .first()
    private val message = "test message"
    private val contacts = setOf("test@example.com", "test2@example.com")
    private val errorMessageForRequestStatusHistory = "The status history was not patched correctly."
    private val dummyContacts = setOf("someContact@example.com", "valid@example.com")

    @BeforeAll
    fun postTestDocuments() {
        documentManagerAccessor.uploadAllTestDocumentsAndAssurePersistence()
    }

    @Test
    fun `post single data request and provide data and check that status has changed to answered`() {
        val mapOfIds =
            apiAccessor.uploadOneCompanyAndEuTaxonomyDataForNonFinancials(
                testCompanyInformation,
                testDataEuTaxonomyNonFinancials,
            )
        val singleDataRequest =
            SingleDataRequest(
                companyIdentifier = mapOfIds["companyId"].toString(),
                dataType = SingleDataRequest.DataType.eutaxonomyMinusNonMinusFinancials,
                reportingPeriods = setOf("2022", "2023"),
                emailOnUpdate = false,
                contacts = dummyContacts,
                message = "This is a test. The current timestamp is ${System.currentTimeMillis()}",
            )
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.PremiumUser)
        val timestampBeforeSingleRequest = retrieveTimeAndWaitOneMillisecond()
        val response = requestControllerApi.postSingleDataRequest(singleDataRequest)
        checkThatAllReportingPeriodsAreTreatedAsExpected(response, 2, 0)
        val newlyStoredRequests = getNewlyStoredRequestsAfterTimestamp(timestampBeforeSingleRequest)
        newlyStoredRequests.forEach {
            assertEquals(
                RequestStatus.Open, it.requestStatus, "The status of a newly stored data request is not 'Open'.",
            )
        }
        uploadDataset(mapOfIds)
        val requestsStoredAfterSingleRequest = getNewlyStoredRequestsAfterTimestamp(timestampBeforeSingleRequest)
        requestsStoredAfterSingleRequest.forEach { checkRequestStatusAfterUpload(it) }
    }

    @Test
    fun `post single data request and provide data for the parent company and check that status has changed to answered`() {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.PremiumUser)
        val sfdrPreparedFixtureProvider = FrameworkTestDataProvider.forFrameworkPreparedFixtures(SfdrData::class.java)
        val (testCompanyWithParent, testParentCompany) =
            listOf(
                "test company with parent lei and existing parent",
                "test company with lei and existing child",
            ).map {
                GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
                    apiAccessor.companyDataControllerApi.postCompany(
                        sfdrPreparedFixtureProvider.getByCompanyName(it).companyInformation,
                    )
                }
            }
        val testSfdrData = apiAccessor.testDataProviderForSfdrData.getTData(1)[0]

        requestControllerApi.postSingleDataRequest(
            SingleDataRequest(
                companyIdentifier = testCompanyWithParent.companyId,
                dataType = SingleDataRequest.DataType.sfdr,
                reportingPeriods = setOf("2023"),
                emailOnUpdate = false,
                contacts = dummyContacts,
                message = "This is a test. The current timestamp is ${System.currentTimeMillis()}",
            ),
        )
        requestControllerApi.postSingleDataRequest(
            SingleDataRequest(
                companyIdentifier = testParentCompany.companyId,
                dataType = SingleDataRequest.DataType.sfdr,
                reportingPeriods = setOf("2023"),
                emailOnUpdate = false,
                contacts = dummyContacts,
                message = "This is a test. The current timestamp is ${System.currentTimeMillis()}",
            ),
        )

        val dataMetaInformation =
            GlobalAuth.withTechnicalUser(TechnicalUser.Uploader) {
                apiAccessor.dataControllerApiForSfdrData.postCompanyAssociatedSfdrData(
                    CompanyAssociatedDataSfdrData(testParentCompany.companyId, "2023", testSfdrData), false,
                )
            }
        assertEquals(getUsersStoredRequestWithLatestCreationTime().requestStatus, RequestStatus.Open)
        GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
            Awaitility.await().atMost(5, TimeUnit.SECONDS).pollDelay(500, TimeUnit.MILLISECONDS).untilAsserted {
                apiAccessor.qaServiceControllerApi.changeQaStatus(dataMetaInformation.dataId, QaStatus.Accepted)
            }
        }
        Awaitility.await().atMost(5, TimeUnit.SECONDS).pollDelay(500, TimeUnit.MILLISECONDS).until {
            getUsersStoredRequestWithLatestCreationTime().requestStatus == RequestStatus.Answered
        }
    }

    private fun uploadDataset(mapOfIds: Map<String, String>) {
        dummyCompanyAssociatedData =
            CompanyAssociatedDataEutaxonomyNonFinancialsData(
                mapOfIds["companyId"].toString(),
                "2022",
                testDataEuTaxonomyNonFinancials,
            )
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        dataController.postCompanyAssociatedEutaxonomyNonFinancialsData(
            dummyCompanyAssociatedData, true,
        )
        Thread.sleep(1000)
    }

    private fun checkRequestStatusAfterUpload(dataRequest: ExtendedStoredDataRequest) {
        if (dataRequest.reportingPeriod == "2022") {
            assertEquals(RequestStatus.Answered, dataRequest.requestStatus)
        } else {
            assertEquals(RequestStatus.Open, dataRequest.requestStatus)
        }
    }

    private fun postSingleDataRequestAsTechnicalUserAndReturnDataRequestId(
        technicalUser: TechnicalUser = TechnicalUser.PremiumUser,
    ): UUID {
        val companyId = getIdForUploadedCompanyWithIdentifiers(permId = System.currentTimeMillis().toString())
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(technicalUser)
        val timestampBeforeSingleRequest = retrieveTimeAndWaitOneMillisecond()
        postStandardSingleDataRequest(companyId)
        val dataRequestId =
            UUID.fromString(
                getNewlyStoredRequestsAfterTimestamp(timestampBeforeSingleRequest)[0].dataRequestId,
            )
        assertStatusForDataRequestId(dataRequestId, RequestStatus.Open)
        return dataRequestId
    }

    private fun authenticateAsTechnicalUserAndAssertThatPatchingOfDataRequestIsForbidden(
        technicalUser: TechnicalUser,
        dataRequestId: UUID,
        requestStatus: RequestStatus?,
        accessStatus: AccessStatus?,
        contacts: Set<String>? = null,
        message: String? = null,
    ) {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(technicalUser)
        val clientException =
            assertThrows<ClientException> {
                requestControllerApi.patchDataRequest(
                    dataRequestId = dataRequestId,
                    dataRequestPatch =
                        DataRequestPatch(
                            requestStatus = requestStatus,
                            accessStatus = accessStatus,
                            contacts = contacts,
                            message = message,
                        ),
                )
            }
        assertEquals("Client error : 403 ", clientException.message)
    }

    @Test
    fun `as a premium user assert that patching the status of a non answered request to resolved is forbidden`() {
        val dataRequestId = postSingleDataRequestAsTechnicalUserAndReturnDataRequestId(TechnicalUser.PremiumUser)

        authenticateAsTechnicalUserAndAssertThatPatchingOfDataRequestIsForbidden(
            technicalUser = TechnicalUser.PremiumUser, dataRequestId = dataRequestId,
            requestStatus = RequestStatus.Resolved, accessStatus = null,
        )

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        patchDataRequestAndAssertNewStatusAndLastModifiedUpdated(dataRequestId, RequestStatus.Answered)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.PremiumUser)
        val requestToResolvedPatch = DataRequestPatch(requestStatus = RequestStatus.Resolved)
        val closedDataRequest = requestControllerApi.patchDataRequest(dataRequestId, requestToResolvedPatch)
        assertEquals(
            RequestStatus.Resolved,
            closedDataRequest.requestStatus,
            "The status of the patched data request is not 'Resolved' although this was expected.",
        )
    }

    @Test
    fun `patch a data request as a reader and check that it is forbidden`() {
        val dataRequestId = postSingleDataRequestAsTechnicalUserAndReturnDataRequestId(TechnicalUser.PremiumUser)
        RequestStatus.entries.forEach {
            authenticateAsTechnicalUserAndAssertThatPatchingOfDataRequestIsForbidden(
                technicalUser = TechnicalUser.Reader, dataRequestId = dataRequestId, requestStatus = it,
                accessStatus = null,
            )
        }
    }

    @Test
    fun `patch an answered but not owned data request to resolved as a premium user and check that it is forbidden`() {
        val dataRequestId = postSingleDataRequestAsTechnicalUserAndReturnDataRequestId(TechnicalUser.Admin)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        patchDataRequestAndAssertNewStatusAndLastModifiedUpdated(dataRequestId, RequestStatus.Answered)

        authenticateAsTechnicalUserAndAssertThatPatchingOfDataRequestIsForbidden(
            technicalUser = TechnicalUser.PremiumUser, dataRequestId = dataRequestId,
            requestStatus = RequestStatus.Resolved, accessStatus = null,
        )
    }

    @Test
    fun `assert that patching a resolved or withdrawn request status is forbidden`() {
        val dataRequestId = postSingleDataRequestAsTechnicalUserAndReturnDataRequestId(TechnicalUser.PremiumUser)
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        patchDataRequestAndAssertNewStatusAndLastModifiedUpdated(dataRequestId, RequestStatus.Withdrawn)

        RequestStatus.entries.forEach {
            authenticateAsTechnicalUserAndAssertThatPatchingOfDataRequestIsForbidden(
                technicalUser = TechnicalUser.PremiumUser, dataRequestId = dataRequestId, requestStatus = it,
                accessStatus = null,
            )
        }

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        patchDataRequestAndAssertNewStatusAndLastModifiedUpdated(dataRequestId, RequestStatus.Resolved)

        RequestStatus.entries.forEach {
            authenticateAsTechnicalUserAndAssertThatPatchingOfDataRequestIsForbidden(
                technicalUser = TechnicalUser.PremiumUser, dataRequestId = dataRequestId, requestStatus = it,
                accessStatus = null,
            )
        }
    }

    @Test
    fun `patch a non existing dataRequestId and assert exception`() {
        val nonExistingDataRequestId = UUID.randomUUID()

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val answeredDataRequestPatch = DataRequestPatch(requestStatus = RequestStatus.Answered)
        val clientException =
            assertThrows<ClientException> {
                requestControllerApi.patchDataRequest(nonExistingDataRequestId, answeredDataRequestPatch)
            }
        val responseBody = (clientException.response as ClientError<*>).body as String

        assertEquals("Client error : 404 ", clientException.message)
        Assertions.assertTrue(
            responseBody.contains("Dataland does not know the Data request ID $nonExistingDataRequestId"),
        )
    }

    @Test
    fun `patch an open or answered data request to withdrawn and assert success`() {
        val dataRequestId = postSingleDataRequestAsTechnicalUserAndReturnDataRequestId(TechnicalUser.PremiumUser)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.PremiumUser)
        val withdrawDataRequestPatch = DataRequestPatch(requestStatus = RequestStatus.Withdrawn)
        val openToWithdrawnDataRequest = requestControllerApi.patchDataRequest(dataRequestId, withdrawDataRequestPatch)
        assertEquals(
            RequestStatus.Withdrawn,
            openToWithdrawnDataRequest.requestStatus,
            "The status of the previously open data request is not 'withdrawn' after patching.",
        )
        assertEquals(
            RequestStatus.Withdrawn, openToWithdrawnDataRequest.dataRequestStatusHistory.last().status,
            errorMessageForRequestStatusHistory,
        )

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        patchDataRequestAndAssertNewStatusAndLastModifiedUpdated(dataRequestId, RequestStatus.Answered)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.PremiumUser)
        val answeredToWithdrawnDataRequest =
            requestControllerApi.patchDataRequest(dataRequestId, withdrawDataRequestPatch)
        assertEquals(
            RequestStatus.Withdrawn,
            answeredToWithdrawnDataRequest.requestStatus,
            "The status of the previously answered data request is not 'withdrawn' after patching.",
        )
        assertEquals(
            RequestStatus.Withdrawn, answeredToWithdrawnDataRequest.dataRequestStatusHistory.last().status,
            errorMessageForRequestStatusHistory,
        )
    }

    @Test
    fun `add a message to an open request do not change the status and assert success`() {
        val dataRequestId = postSingleDataRequestAsTechnicalUserAndReturnDataRequestId(TechnicalUser.PremiumUser)
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.PremiumUser)
        val messageDataRequestPatch = DataRequestPatch(contacts = contacts, message = message)
        val newMessageDataRequest = requestControllerApi.patchDataRequest(dataRequestId, messageDataRequestPatch)

        assertEquals(
            message, newMessageDataRequest.messageHistory.last().message,
            "The message was not patched correctly.",
        )
        assertEquals(
            contacts, newMessageDataRequest.messageHistory.last().contacts,
            "The contacts were not patched correctly.",
        )
    }

    @Test
    fun `add a message to an answered request and change the status to open and assert success`() {
        val dataRequestId = postSingleDataRequestAsTechnicalUserAndReturnDataRequestId(TechnicalUser.PremiumUser)
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        patchDataRequestAndAssertNewStatusAndLastModifiedUpdated(dataRequestId, RequestStatus.Answered)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.PremiumUser)
        val addMessageAndOpenDataRequestPatch =
            DataRequestPatch(
                requestStatus = RequestStatus.Open,
                contacts = contacts,
                message = message,
            )
        val newMessageAndOpenDataRequest =
            requestControllerApi.patchDataRequest(dataRequestId, addMessageAndOpenDataRequestPatch)
        assertEquals(
            message, newMessageAndOpenDataRequest.messageHistory.last().message,
            "The message was not patched correctly.",
        )
        assertEquals(
            contacts, newMessageAndOpenDataRequest.messageHistory.last().contacts,
            "The contacts were not patched correctly.",
        )
        assertEquals(
            RequestStatus.Open, newMessageAndOpenDataRequest.dataRequestStatusHistory.last().status,
            errorMessageForRequestStatusHistory,
        )
        assertEquals(RequestStatus.Open, newMessageAndOpenDataRequest.requestStatus)
    }

    @Test
    fun `patch the message history of an not open request and assert that it is forbidden`() {
        val dataRequestId = postSingleDataRequestAsTechnicalUserAndReturnDataRequestId(TechnicalUser.PremiumUser)

        RequestStatus.entries.filter { it != RequestStatus.Open }.forEach {
            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
            patchDataRequestAndAssertNewStatusAndLastModifiedUpdated(dataRequestId, it)

            authenticateAsTechnicalUserAndAssertThatPatchingOfDataRequestIsForbidden(
                technicalUser = TechnicalUser.PremiumUser, dataRequestId = dataRequestId, requestStatus = null,
                accessStatus = null, contacts = contacts, message = message,
            )
        }
    }
}
