package org.dataland.e2etests.tests.dataSourcingService

import org.dataland.dataSourcingService.openApiClient.infrastructure.ClientException
import org.dataland.dataSourcingService.openApiClient.model.RequestPriority
import org.dataland.dataSourcingService.openApiClient.model.RequestState
import org.dataland.dataSourcingService.openApiClient.model.SingleRequest
import org.dataland.dataSourcingService.openApiClient.model.StoredRequest
import org.dataland.e2etests.auth.GlobalAuth
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import java.time.Instant

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RequestControllerTest {
    private val apiAccessor = ApiAccessor()
    private lateinit var dummyRequest: SingleRequest

    @BeforeEach
    fun setup() {
        val companyId =
            GlobalAuth.withTechnicalUser(TechnicalUser.Uploader) {
                apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
            }
        dummyRequest = SingleRequest(companyId, SingleRequest.DataType.sfdr, "2023", "dummy request")
    }

    private fun postRequestForUserAndVerifyRetrieval(request: SingleRequest) {
        val user = TechnicalUser.Reader
        val timeBeforeUpload = Instant.now().toEpochMilli()
        val requestId = apiAccessor.dataSourcingRequestControllerApi.createRequest(request, user.technicalUserId).requestId

        val storedRequest =
            GlobalAuth.withTechnicalUser(TechnicalUser.Reader) {
                apiAccessor.dataSourcingRequestControllerApi.getRequest(requestId)
            }

        assertEquals(request.companyIdentifier, storedRequest.companyId)
        assertEquals(request.dataType, storedRequest.dataType)
        assertEquals(request.reportingPeriod, storedRequest.reportingPeriod)
        assertEquals(request.memberComment, storedRequest.memberComment)

        assertTrue(timeBeforeUpload < storedRequest.creationTimeStamp)
        assertTrue(storedRequest.creationTimeStamp < Instant.now().toEpochMilli())
        assertEquals(storedRequest.creationTimeStamp, storedRequest.lastModifiedDate)
        assertEquals(RequestState.Open, storedRequest.state)
    }

    @Test
    fun `post a request and verify that it can be retrieved`() {
        GlobalAuth.withTechnicalUser(TechnicalUser.Reader) {
            postRequestForUserAndVerifyRetrieval(dummyRequest)
        }
    }

    @Test
    fun `post a request in the name of another user as admin and verify that it can be retrieved`() {
        GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
            postRequestForUserAndVerifyRetrieval(dummyRequest)
        }
    }

    @Test
    fun `post a request with invalid company ID and verify that it is rejected`() {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        val invalidRequest = SingleRequest("invalidCompanyId", SingleRequest.DataType.sfdr, "2023", "dummy request")
        val exception =
            assertThrows<ClientException> {
                apiAccessor.dataSourcingRequestControllerApi.createRequest(invalidRequest)
            }
        assertEquals(404, exception.statusCode)
    }

    @Test
    fun `post a request in the name of another user as reader and verify that it is forbidden`() {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        val exception =
            assertThrows<ClientException> {
                apiAccessor.dataSourcingRequestControllerApi
                    .createRequest(dummyRequest, TechnicalUser.Reviewer.technicalUserId)
            }
        assertEquals(403, exception.statusCode)
    }

    @Test
    fun `patch the state of a request and verify that the changes are saved`() {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)

        val initialRequestId = apiAccessor.dataSourcingRequestControllerApi.createRequest(dummyRequest).requestId
        val initialRequest = apiAccessor.dataSourcingRequestControllerApi.getRequest(initialRequestId)
        apiAccessor.dataSourcingRequestControllerApi.patchRequestState(initialRequest.id, RequestState.Processing)
        val patchedRequest = apiAccessor.dataSourcingRequestControllerApi.getRequest(initialRequest.id)

        assertTrue(patchedRequest.lastModifiedDate > initialRequest.lastModifiedDate)
        assertEquals(RequestState.Processing, patchedRequest.state)
        assertEquals(
            initialRequest.copy(
                state = RequestState.Processing,
                lastModifiedDate = patchedRequest.lastModifiedDate,
                dataSourcingEntityId = patchedRequest.dataSourcingEntityId,
            ),
            patchedRequest,
        )
    }

    @Test
    fun `patch the priority of a request and verify that the changes are saved`() {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)

        val initialRequestId = apiAccessor.dataSourcingRequestControllerApi.createRequest(dummyRequest).requestId
        val initialRequest = apiAccessor.dataSourcingRequestControllerApi.getRequest(initialRequestId)
        val comment = "Changing priority to urgent"
        apiAccessor.dataSourcingRequestControllerApi.patchRequestPriority(
            initialRequest.id,
            RequestPriority.Urgent,
            comment,
        )
        val patchedRequest = apiAccessor.dataSourcingRequestControllerApi.getRequest(initialRequest.id)

        assertTrue(patchedRequest.lastModifiedDate > initialRequest.lastModifiedDate)
        assertEquals(RequestPriority.Urgent, patchedRequest.requestPriority)
        assertEquals(comment, patchedRequest.adminComment)
    }

    @Test
    fun `verify that historization works for data requests`() {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        val requestId = apiAccessor.dataSourcingRequestControllerApi.createRequest(dummyRequest).requestId

        lateinit var requestHistory: List<StoredRequest>

        GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
            apiAccessor.dataSourcingRequestControllerApi.patchRequestState(requestId, RequestState.Processing)
            requestHistory = apiAccessor.dataSourcingRequestControllerApi.getRequestHistoryById(requestId)
        }

        assertEquals(2, requestHistory.size)
        assertEquals(RequestState.Open, requestHistory[0].state)
        assertEquals(RequestState.Processing, requestHistory[1].state)
    }
}
