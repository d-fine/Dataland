package org.dataland.e2etests.tests.communityManager

import org.dataland.communitymanager.openApiClient.infrastructure.ClientError
import org.dataland.communitymanager.openApiClient.infrastructure.ClientException
import org.dataland.communitymanager.openApiClient.model.ExtendedStoredDataRequest
import org.dataland.communitymanager.openApiClient.model.RequestStatus
import org.dataland.communitymanager.openApiClient.model.SingleDataRequest
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEutaxonomyNonFinancialsData
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.communityManager.assertStatusForDataRequestId
import org.dataland.e2etests.utils.communityManager.checkThatAllReportingPeriodsAreTreatedAsExpected
import org.dataland.e2etests.utils.communityManager.getIdForUploadedCompanyWithIdentifiers
import org.dataland.e2etests.utils.communityManager.getNewlyStoredRequestsAfterTimestamp
import org.dataland.e2etests.utils.communityManager.patchDataRequestAndAssertNewStatusAndLastModifiedUpdated
import org.dataland.e2etests.utils.communityManager.postStandardSingleDataRequest
import org.dataland.e2etests.utils.communityManager.retrieveTimeAndWaitOneMillisecond
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataRequestUploadListenerTest {
    val apiAccessor = ApiAccessor()
    val jwtHelper = JwtAuthenticationHelper()
    private val requestControllerApi = apiAccessor.requestControllerApi
    private val dataController = apiAccessor.dataControllerApiForEuTaxonomyNonFinancials
    private lateinit var dummyCompanyAssociatedData: CompanyAssociatedDataEutaxonomyNonFinancialsData
    private val testDataEuTaxonomyNonFinancials = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
        .getTData(1).first()

    private val testCompanyInformation = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
        .getCompanyInformationWithoutIdentifiers(1).first()

    @Test
    fun `post single data request and provide data and check that status has changed to answered`() {
        val mapOfIds = apiAccessor.uploadOneCompanyAndEuTaxonomyDataForNonFinancials(
            testCompanyInformation,
            testDataEuTaxonomyNonFinancials,
        )
        val singleDataRequest = SingleDataRequest(
            companyIdentifier = mapOfIds["companyId"].toString(),
            dataType = SingleDataRequest.DataType.eutaxonomyMinusNonMinusFinancials,
            reportingPeriods = setOf("2022", "2023"),
            contacts = setOf("someContact@example.com", "valid@example.com"),
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
        val dataRequestId = UUID.fromString(
            getNewlyStoredRequestsAfterTimestamp(timestampBeforeSingleRequest)[0].dataRequestId,
        )
        assertStatusForDataRequestId(dataRequestId, RequestStatus.Open)
        return dataRequestId
    }

    private fun authenticateAsTechnicalUserAndAssertThatPatchingOfDataRequestIsForbidden(
        technicalUser: TechnicalUser,
        dataRequestId: UUID,
        requestStatus: RequestStatus?,
        contacts: Set<String>? = null,
        message: String? = null,
    ) {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(technicalUser)
        val clientException = assertThrows<ClientException> {
            requestControllerApi.patchDataRequest(dataRequestId, requestStatus, contacts, message)
        }
        assertEquals("Client error : 403 ", clientException.message)
    }

    @Test
    fun `patch your own data request as a premium user to closed and check that this is allowed if answered before`() {
        val dataRequestId = postSingleDataRequestAsTechnicalUserAndReturnDataRequestId(TechnicalUser.PremiumUser)

        authenticateAsTechnicalUserAndAssertThatPatchingOfDataRequestIsForbidden(
            TechnicalUser.PremiumUser, dataRequestId, RequestStatus.Closed,
        )

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        patchDataRequestAndAssertNewStatusAndLastModifiedUpdated(dataRequestId, RequestStatus.Answered)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.PremiumUser)
        val closedDataRequest = requestControllerApi.patchDataRequest(dataRequestId, RequestStatus.Closed)
        assertEquals(
            RequestStatus.Closed,
            closedDataRequest.requestStatus,
            "The status of the patched data request is not 'Closed' although this was expected.",
        )
    }

    @Test
    fun `patch a data request as a reader and check that it is forbidden`() {
        val dataRequestId = postSingleDataRequestAsTechnicalUserAndReturnDataRequestId(TechnicalUser.PremiumUser)
        RequestStatus.entries.forEach {
            authenticateAsTechnicalUserAndAssertThatPatchingOfDataRequestIsForbidden(
                TechnicalUser.Reader, dataRequestId, it,
            )
        }
    }

    @Test
    fun `patch an answered but not owned data request to closed as a premium user and check that it is forbidden`() {
        val dataRequestId = postSingleDataRequestAsTechnicalUserAndReturnDataRequestId(TechnicalUser.Admin)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        patchDataRequestAndAssertNewStatusAndLastModifiedUpdated(dataRequestId, RequestStatus.Answered)

        authenticateAsTechnicalUserAndAssertThatPatchingOfDataRequestIsForbidden(
            TechnicalUser.PremiumUser, dataRequestId, RequestStatus.Closed,
        )
    }

    @Test
    fun `patch own open or closed request as premium user and check that its forbidden except open to withdrawn`() {
        val dataRequestId = postSingleDataRequestAsTechnicalUserAndReturnDataRequestId(TechnicalUser.PremiumUser)

        RequestStatus.entries.filter { it != RequestStatus.Withdrawn }
            .forEach {
                authenticateAsTechnicalUserAndAssertThatPatchingOfDataRequestIsForbidden(
                    TechnicalUser.PremiumUser, dataRequestId, it,
                )
            }

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        patchDataRequestAndAssertNewStatusAndLastModifiedUpdated(dataRequestId, RequestStatus.Closed)

        RequestStatus.entries.forEach {
            authenticateAsTechnicalUserAndAssertThatPatchingOfDataRequestIsForbidden(
                TechnicalUser.PremiumUser, dataRequestId, it,
            )
        }
    }

    @Test
    fun `patch a non existing dataRequestId and assert exception`() {
        val nonExistingDataRequestId = UUID.randomUUID()

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val clientException = assertThrows<ClientException> {
            requestControllerApi.patchDataRequest(nonExistingDataRequestId, RequestStatus.Answered)
        }
        val responseBody = (clientException.response as ClientError<*>).body as String

        assertEquals("Client error : 404 ", clientException.message)
        Assertions.assertTrue(
            responseBody.contains("Dataland does not know the Data request ID $nonExistingDataRequestId"),
        )
    }

    @Test
    fun `patch a open or answered data request to withdrawn and assert success`() {
        val dataRequestId = postSingleDataRequestAsTechnicalUserAndReturnDataRequestId(TechnicalUser.PremiumUser)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.PremiumUser)
        val openToWithdrawnDataRequest = requestControllerApi.patchDataRequest(dataRequestId, RequestStatus.Withdrawn)
        assertEquals(
            RequestStatus.Withdrawn,
            openToWithdrawnDataRequest.requestStatus,
            "The status of the previously open data request is not 'withdrawn' after patching.",
        )

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        patchDataRequestAndAssertNewStatusAndLastModifiedUpdated(dataRequestId, RequestStatus.Answered)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.PremiumUser)
        val answeredToWithdrawnDataRequest =
            requestControllerApi.patchDataRequest(dataRequestId, RequestStatus.Withdrawn)
        assertEquals(
            RequestStatus.Withdrawn,
            answeredToWithdrawnDataRequest.requestStatus,
            "The status of the previously answered data request is not 'withdrawn' after patching.",
        )
    }

    @Test
    fun `add a message to an open request do not change the status and assert success`() {
        val dataRequestId = postSingleDataRequestAsTechnicalUserAndReturnDataRequestId(TechnicalUser.PremiumUser)
        val message = "test message"
        val contacts = setOf("test@example.com", "test2@example.com")
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.PremiumUser)
        val newMessageDataRequest = requestControllerApi.patchDataRequest(dataRequestId, null, contacts, message)

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
        val message = "test message"
        val contacts = setOf("test@example.com", "test2@example.com")
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.PremiumUser)
        val newMessageAndOpenDataRequest =
            requestControllerApi.patchDataRequest(dataRequestId, RequestStatus.Open, contacts, message)
        assertEquals(
            message, newMessageAndOpenDataRequest.messageHistory.last().message,
            "The message was not patched correctly.",
        )
        assertEquals(
            contacts, newMessageAndOpenDataRequest.messageHistory.last().contacts,
            "The contacts were not patched correctly.",
        )
        assertEquals(RequestStatus.Open, newMessageAndOpenDataRequest.requestStatus)
    }

    @Test
    fun `patch the message history of an not open request and assert that it is forbidden`() {
        val dataRequestId = postSingleDataRequestAsTechnicalUserAndReturnDataRequestId(TechnicalUser.PremiumUser)
        val message = "test message"
        val contacts = setOf("test@example.com")

        RequestStatus.entries.filter { it != RequestStatus.Open }.forEach {
            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
            patchDataRequestAndAssertNewStatusAndLastModifiedUpdated(dataRequestId, it)

            authenticateAsTechnicalUserAndAssertThatPatchingOfDataRequestIsForbidden(
                TechnicalUser.PremiumUser, dataRequestId, null, contacts, message,
            )
        }
    }
}
