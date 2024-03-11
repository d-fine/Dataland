package org.dataland.e2etests.tests.communityManager

import org.dataland.communitymanager.openApiClient.infrastructure.ClientError
import org.dataland.communitymanager.openApiClient.infrastructure.ClientException
import org.dataland.communitymanager.openApiClient.model.RequestStatus
import org.dataland.communitymanager.openApiClient.model.SingleDataRequest
import org.dataland.communitymanager.openApiClient.model.StoredDataRequest
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEutaxonomyNonFinancialsData
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.assertStatusForDataRequestId
import org.dataland.e2etests.utils.communityManager.checkThatAllReportingPeriodsAreTreatedAsExpected
import org.dataland.e2etests.utils.communityManager.postStandardSingleDataRequest
import org.dataland.e2etests.utils.getIdForUploadedCompanyWithIdentifiers
import org.dataland.e2etests.utils.getNewlyStoredRequestsAfterTimestamp
import org.dataland.e2etests.utils.patchDataRequestAndAssertNewStatusAndLastModifiedUpdated
import org.dataland.e2etests.utils.retrieveTimeAndWaitOneMillisecond
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataRequestUpdaterTest {
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
            contacts = setOf("someContact@webserver.de", "valid@e.mail"),
            message = "This is a test. The current timestamp is ${System.currentTimeMillis()}",
        )
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.PremiumUser)
        val timestampBeforeSingleRequest = retrieveTimeAndWaitOneMillisecond()
        val response = requestControllerApi.postSingleDataRequest(singleDataRequest)
        checkThatAllReportingPeriodsAreTreatedAsExpected(response, 2, 0)
        val newlyStoredRequests = getNewlyStoredRequestsAfterTimestamp(timestampBeforeSingleRequest)
        newlyStoredRequests.forEach {
            assertEquals(
                RequestStatus.open, it.requestStatus, "The status of a newly stored data request is not 'Open'.",
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

    private fun checkRequestStatusAfterUpload(dataRequest: StoredDataRequest) {
        if (dataRequest.reportingPeriod == "2022") {
            assertEquals(RequestStatus.answered, dataRequest.requestStatus)
        } else {
            assertEquals(RequestStatus.open, dataRequest.requestStatus)
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
        assertStatusForDataRequestId(dataRequestId, RequestStatus.open)
        return dataRequestId
    }

    private fun authenticateAsTechnicalUserAndAssertThatPatchingStatusOfDataRequestIsForbidden(
        technicalUser: TechnicalUser,
        dataRequestId: UUID,
        requestStatus: RequestStatus,
    ) {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(technicalUser)
        val clientException = assertThrows<ClientException> {
            requestControllerApi.patchDataRequestStatus(dataRequestId, requestStatus)
        }
        assertEquals("Client error : 403 ", clientException.message)
    }

    @Test
    fun `patch your own data request as a premium user to closed and check that this is allowed if answered before`() {
        val dataRequestId = postSingleDataRequestAsTechnicalUserAndReturnDataRequestId(TechnicalUser.PremiumUser)

        authenticateAsTechnicalUserAndAssertThatPatchingStatusOfDataRequestIsForbidden(
            TechnicalUser.PremiumUser, dataRequestId, RequestStatus.closed,
        )

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        patchDataRequestAndAssertNewStatusAndLastModifiedUpdated(dataRequestId, RequestStatus.answered)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.PremiumUser)
        val closedDataRequest = requestControllerApi.patchDataRequestStatus(dataRequestId, RequestStatus.closed)
        assertEquals(
            RequestStatus.closed,
            closedDataRequest.requestStatus,
            "The status of the patched data request is not 'Closed' although this was expected.",
        )
    }

    @Test
    fun `patch a data request as a reader and check that it is forbidden`() {
        val dataRequestId = postSingleDataRequestAsTechnicalUserAndReturnDataRequestId(TechnicalUser.PremiumUser)
        RequestStatus.entries.forEach {
            authenticateAsTechnicalUserAndAssertThatPatchingStatusOfDataRequestIsForbidden(
                TechnicalUser.Reader, dataRequestId, it,
            )
        }
    }

    @Test
    fun `patch an answered but not owned data request to closed as a premium user and check that it is forbidden`() {
        val dataRequestId = postSingleDataRequestAsTechnicalUserAndReturnDataRequestId(TechnicalUser.Admin)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        patchDataRequestAndAssertNewStatusAndLastModifiedUpdated(dataRequestId, RequestStatus.answered)

        authenticateAsTechnicalUserAndAssertThatPatchingStatusOfDataRequestIsForbidden(
            TechnicalUser.PremiumUser, dataRequestId, RequestStatus.closed,
        )
    }

    @Test
    fun `patch your own open or closed data request as a premium user and check that both is forbidden`() {
        val dataRequestId = postSingleDataRequestAsTechnicalUserAndReturnDataRequestId(TechnicalUser.PremiumUser)

        RequestStatus.entries.forEach {
            authenticateAsTechnicalUserAndAssertThatPatchingStatusOfDataRequestIsForbidden(
                TechnicalUser.PremiumUser, dataRequestId, it,
            )
        }

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        patchDataRequestAndAssertNewStatusAndLastModifiedUpdated(dataRequestId, RequestStatus.closed)

        RequestStatus.entries.forEach {
            authenticateAsTechnicalUserAndAssertThatPatchingStatusOfDataRequestIsForbidden(
                TechnicalUser.PremiumUser, dataRequestId, it,
            )
        }
    }

    @Test
    fun `patch a non existing dataRequestId and assert exception`() {
        val nonExistingDataRequestId = UUID.randomUUID()

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val clientException = assertThrows<ClientException> {
            requestControllerApi.patchDataRequestStatus(nonExistingDataRequestId, RequestStatus.answered)
        }
        val responseBody = (clientException.response as ClientError<*>).body as String

        assertEquals("Client error : 404 ", clientException.message)
        Assertions.assertTrue(
            responseBody.contains("Dataland does not know the Data request ID $nonExistingDataRequestId"),
        )
    }
}
