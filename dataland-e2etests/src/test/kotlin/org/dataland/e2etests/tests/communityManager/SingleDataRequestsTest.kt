package org.dataland.e2etests.tests.communityManager

import org.dataland.communitymanager.openApiClient.api.RequestControllerApi
import org.dataland.communitymanager.openApiClient.infrastructure.ClientError
import org.dataland.communitymanager.openApiClient.infrastructure.ClientException
import org.dataland.communitymanager.openApiClient.model.RequestStatus
import org.dataland.communitymanager.openApiClient.model.SingleDataRequest
import org.dataland.e2etests.BASE_PATH_TO_COMMUNITY_MANAGER
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.assertStatusForDataRequestId
import org.dataland.e2etests.utils.checkErrorMessageForInvalidIdentifiers
import org.dataland.e2etests.utils.patchDataRequestAndAssertNewStatus
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SingleDataRequestsTest {

    val jwtHelper = JwtAuthenticationHelper()
    private val requestControllerApi = RequestControllerApi(BASE_PATH_TO_COMMUNITY_MANAGER)

    @Test
    fun `post single data request and check if retrieval of stored requests via their IDs works as expected`() {
        val stringThatMatchesThePermIdRegex = System.currentTimeMillis().toString()
        val singleDataRequest = SingleDataRequest(
            companyIdentifier = stringThatMatchesThePermIdRegex,
            frameworkName = SingleDataRequest.FrameworkName.lksg,
            listOfReportingPeriods = listOf("2022", "2023"),
            contactList = listOf("someContact@webserver.de", "simpleString"),
            message = "This is a test. The current timestamp is ${System.currentTimeMillis()}",
        )
        val allStoredDataRequests = requestControllerApi.postSingleDataRequest(singleDataRequest)
        Assertions.assertEquals(singleDataRequest.listOfReportingPeriods.size, allStoredDataRequests.size)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        for (storedDataRequest in allStoredDataRequests) {
            val retrievedDataRequest = requestControllerApi.getDataRequestById(
                UUID.fromString(storedDataRequest.dataRequestId),
            )
            Assertions.assertEquals(storedDataRequest, retrievedDataRequest)
        }
    }

    @Test
    fun `post single data request for invalid companyId and assert exception`() {
        val invalidCompanyIdentifier = "a"
        val invalidSingleDataRequest = SingleDataRequest(
            companyIdentifier = invalidCompanyIdentifier,
            frameworkName = SingleDataRequest.FrameworkName.lksg,
            listOfReportingPeriods = listOf("2022"),
        )
        val clientException = assertThrows<ClientException> {
            requestControllerApi.postSingleDataRequest(invalidSingleDataRequest)
        }
        checkErrorMessageForInvalidIdentifiers(clientException)
    }

    @Test
    fun `post a single data request and check if patching it changes its status accordingly`() {
        val stringThatMatchesThePermIdRegex = System.currentTimeMillis().toString()
        val singleDataRequest = SingleDataRequest(
            companyIdentifier = stringThatMatchesThePermIdRegex,
            frameworkName = SingleDataRequest.FrameworkName.lksg,
            listOfReportingPeriods = listOf("2022"),
        )
        val storedDataRequest = requestControllerApi.postSingleDataRequest(singleDataRequest).first()
        val storedDataRequestId = UUID.fromString(storedDataRequest.dataRequestId)
        Assertions.assertEquals(RequestStatus.open, storedDataRequest.requestStatus)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)

        assertStatusForDataRequestId(storedDataRequestId, RequestStatus.open)

        patchDataRequestAndAssertNewStatus(storedDataRequestId, RequestStatus.resolved)

        patchDataRequestAndAssertNewStatus(storedDataRequestId, RequestStatus.open)
    }

    @Test
    fun `patch a non existing dataRequestId and assert exception`() {
        val nonExistingDataRequestId = UUID.randomUUID()

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val clientException = assertThrows<ClientException> {
            requestControllerApi.patchDataRequest(nonExistingDataRequestId, RequestStatus.resolved)
        }
        val responseBody = (clientException.response as ClientError<*>).body as String

        Assertions.assertEquals("Client error : 404 ", clientException.message)
        Assertions.assertTrue(
            responseBody.contains("Dataland does not know the Data request ID $nonExistingDataRequestId"),
        )
    }

    @Test
    fun `patch data request as an uploader and assert that it is forbidden`() {
        val stringThatMatchesThePermIdRegex = System.currentTimeMillis().toString()
        val singleDataRequest = SingleDataRequest(
            companyIdentifier = stringThatMatchesThePermIdRegex,
            frameworkName = SingleDataRequest.FrameworkName.lksg,
            listOfReportingPeriods = listOf("2022"),
        )
        val storedDataRequest = requestControllerApi.postSingleDataRequest(singleDataRequest).first()
        val storedDataRequestId = UUID.fromString(storedDataRequest.dataRequestId)
        Assertions.assertEquals(RequestStatus.open, storedDataRequest.requestStatus)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)

        val clientException = assertThrows<ClientException> {
            requestControllerApi.patchDataRequest(storedDataRequestId, RequestStatus.resolved)
        }
        Assertions.assertEquals("Client error : 403 ", clientException.message)
    }

    // TODO test last get endpoint
}
