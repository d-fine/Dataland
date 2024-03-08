package org.dataland.e2etests.tests.communityManager

import org.dataland.communitymanager.openApiClient.api.RequestControllerApi
import org.dataland.communitymanager.openApiClient.infrastructure.ClientError
import org.dataland.communitymanager.openApiClient.infrastructure.ClientException
import org.dataland.communitymanager.openApiClient.model.RequestStatus
import org.dataland.communitymanager.openApiClient.model.SingleDataRequest
import org.dataland.communitymanager.openApiClient.model.SingleDataRequestResponse
import org.dataland.e2etests.BASE_PATH_TO_COMMUNITY_MANAGER
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.assertStatusForDataRequestId
import org.dataland.e2etests.utils.check400ClientExceptionErrorMessage
import org.dataland.e2etests.utils.checkThatRequestForFrameworkReportingPeriodAndIdentifierExistsExactlyOnce
import org.dataland.e2etests.utils.checkThatTheAmountOfNewlyStoredRequestsIsAsExpected
import org.dataland.e2etests.utils.communityManager.checkThatAllReportingPeriodsAreTreatedAsExpected
import org.dataland.e2etests.utils.generateRandomLei
import org.dataland.e2etests.utils.generateRandomPermId
import org.dataland.e2etests.utils.getIdForUploadedCompanyWithIdentifiers
import org.dataland.e2etests.utils.getNewlyStoredRequestsAfterTimestamp
import org.dataland.e2etests.utils.patchDataRequestAndAssertNewStatusAndLastModifiedUpdated
import org.dataland.e2etests.utils.retrieveTimeAndWaitOneMillisecond
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SingleDataRequestsTest {

    val apiAccessor = ApiAccessor()
    val jwtHelper = JwtAuthenticationHelper()
    private val requestControllerApi = RequestControllerApi(BASE_PATH_TO_COMMUNITY_MANAGER)

    @BeforeEach
    fun authenticateAsPremiumUser() {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.PremiumUser)
    }

    @Test
    fun `post single data request for multiple reporting periods and check stored requests`() {
        val stringThatMatchesThePermIdRegex = System.currentTimeMillis().toString()
        val companyId = getIdForUploadedCompanyWithIdentifiers(permId = stringThatMatchesThePermIdRegex)
        val reportingPeriods = setOf("2022", "2023")
        val singleDataRequest = SingleDataRequest(
            companyIdentifier = stringThatMatchesThePermIdRegex,
            dataType = SingleDataRequest.DataType.lksg,
            reportingPeriods = reportingPeriods,
            contacts = setOf("someContact@webserver.de", "simpleString@some.thing"),
            message = "This is a test. The current timestamp is ${System.currentTimeMillis()}",
        )
        val timestampBeforeSingleRequest = retrieveTimeAndWaitOneMillisecond()
        val response = requestControllerApi.postSingleDataRequest(singleDataRequest)
        checkThatAllReportingPeriodsAreTreatedAsExpected(
            singleDataRequestResponse = response,
            expectedNumberOfStoredReportingPeriods = reportingPeriods.size,
            expectedNumberOfDuplicateReportingPeriods = 0,
        )
        val newlyStoredRequests = getNewlyStoredRequestsAfterTimestamp(timestampBeforeSingleRequest)
        checkThatTheAmountOfNewlyStoredRequestsIsAsExpected(newlyStoredRequests, reportingPeriods.size)
        newlyStoredRequests.forEach {
            assertEquals(
                companyId, it.datalandCompanyId, "The company ID in a stored data request is not as expected.",
            )
        }
        reportingPeriods.forEach {
            checkThatRequestForFrameworkReportingPeriodAndIdentifierExistsExactlyOnce(
                newlyStoredRequests, SingleDataRequest.DataType.lksg.value, it, companyId,
            )
        }
    }

    @Test
    fun `post single data request for companyId with invalid format and assert exception`() {
        val invalidCompanyIdentifier = "invalid-identifier-${Instant.now().toEpochMilli()}"
        val invalidSingleDataRequest = SingleDataRequest(
            companyIdentifier = invalidCompanyIdentifier,
            dataType = SingleDataRequest.DataType.lksg,
            reportingPeriods = setOf("2022"),
        )
        val clientException = assertThrows<ClientException> {
            requestControllerApi.postSingleDataRequest(invalidSingleDataRequest)
        }
        check400ClientExceptionErrorMessage(clientException)
        val responseBody = (clientException.response as ClientError<*>).body as String
        assertTrue(responseBody.contains("The specified company is unknown to Dataland"))
        assertTrue(
            responseBody.contains(
                "The company with identifier: $invalidCompanyIdentifier is unknown to Dataland",
            ),
        )
    }

    @Test
    fun `post single data request for a companyId which is unknown to Dataland and assert exception`() {
        val unknownCompanyId = UUID.randomUUID().toString()
        val singleDataRequest = SingleDataRequest(
            companyIdentifier = unknownCompanyId,
            dataType = SingleDataRequest.DataType.lksg,
            reportingPeriods = setOf("2022"),
        )
        val clientException = assertThrows<ClientException> {
            requestControllerApi.postSingleDataRequest(singleDataRequest)
        }
        assertEquals("Client error : 404 ", clientException.message)

        val responseBody = (clientException.response as ClientError<*>).body as String
        assertTrue(responseBody.contains("resource-not-found"))
    }

    @Test
    fun `post a single data request with a valid Dataland companyId and assure that it is stored as expected`() {
        val companyIdOfNewCompany =
            apiAccessor.uploadOneCompanyWithIdentifiers(permId = generateRandomPermId())!!.actualStoredCompany.companyId
        val singleDataRequest = SingleDataRequest(
            companyIdentifier = companyIdOfNewCompany,
            dataType = SingleDataRequest.DataType.lksg,
            reportingPeriods = setOf("2022"),
        )
        val timestampBeforeSingleRequest = retrieveTimeAndWaitOneMillisecond()
        val response = requestControllerApi.postSingleDataRequest(singleDataRequest)
        checkThatAllReportingPeriodsAreTreatedAsExpected(
            singleDataRequestResponse = response,
            expectedNumberOfStoredReportingPeriods = 1,
            expectedNumberOfDuplicateReportingPeriods = 0,
        )
        val newlyStoredRequests = getNewlyStoredRequestsAfterTimestamp(timestampBeforeSingleRequest)
        checkThatTheAmountOfNewlyStoredRequestsIsAsExpected(newlyStoredRequests, 1)
        assertEquals(
            companyIdOfNewCompany,
            newlyStoredRequests[0].datalandCompanyId,
            "The company ID of the newly stored data request does not match the expected one.",
        )
        assertEquals(
            RequestStatus.open,
            newlyStoredRequests[0].requestStatus,
            "The new data request is not stored with status 'Open'.",
        )
    }

    private fun postStandardSingleDataRequest(
        companyIdentifier: String,
        contacts: Set<String>? = null,
        message: String? = null,
    ): SingleDataRequestResponse {
        return requestControllerApi.postSingleDataRequest(
            SingleDataRequest(
                companyIdentifier = companyIdentifier,
                dataType = SingleDataRequest.DataType.sfdr,
                reportingPeriods = setOf("2022"),
                contacts = contacts,
                message = message,
            ),
        )
    }

    @Test
    fun `post single data requests with message but invalid email addresses in contact lists and assert exception`() {
        val validLei = generateRandomLei()
        apiAccessor.uploadOneCompanyWithIdentifiers(lei = validLei)

        val contactListsThatContainInvalidEmailAddresses =
            listOf(listOf(""), listOf(" "), listOf("invalidMail@", "validMail@somemailabc.abc"))
        contactListsThatContainInvalidEmailAddresses.forEach {
            val clientException = assertThrows<ClientException> {
                postStandardSingleDataRequest(validLei, it.toSet(), "Dummy test message.")
            }
            check400ClientExceptionErrorMessage(clientException)
            val responseBody = (clientException.response as ClientError<*>).body as String
            assertTrue(responseBody.contains("Invalid email address \\\"${it[0]}\\\""))
            assertTrue(
                responseBody.contains(
                    "The email address \\\"${it[0]}\\\" you have provided has an invalid format.",
                ),
            )
        }
    }

    @Test
    fun `post single data requests with message but missing email addresses in contact lists and assert exception`() {
        val validLei = generateRandomLei()
        apiAccessor.uploadOneCompanyWithIdentifiers(lei = validLei)

        val contactSetsThatDontHaveEmailAddresses =
            listOf<Set<String>?>(null, setOf())
        contactSetsThatDontHaveEmailAddresses.forEach {
            val clientException = assertThrows<ClientException> {
                postStandardSingleDataRequest(validLei, it, "Dummy test message.")
            }
            check400ClientExceptionErrorMessage(clientException)
            val responseBody = (clientException.response as ClientError<*>).body as String
            assertTrue(responseBody.contains("No recipients provided for the message"))
            assertTrue(
                responseBody.contains(
                    "You have provided a message, but no recipients. " +
                        "Without at least one valid email address being provided no message can be forwarded.",
                ),
            )
        }
    }

    @Test
    fun `post a single data requests without a message but with valid email address in contact list`() {
        val companyId = getIdForUploadedCompanyWithIdentifiers(lei = generateRandomLei())
        val emailAddress = "test@someprovider.abc"
        val timestampBeforeSingleRequest = retrieveTimeAndWaitOneMillisecond()
        val response = postStandardSingleDataRequest(companyId, setOf(emailAddress))
        checkThatAllReportingPeriodsAreTreatedAsExpected(
            singleDataRequestResponse = response,
            expectedNumberOfStoredReportingPeriods = 1,
            expectedNumberOfDuplicateReportingPeriods = 0,
        )
        val newlyStoredRequests = getNewlyStoredRequestsAfterTimestamp(timestampBeforeSingleRequest)
        checkThatTheAmountOfNewlyStoredRequestsIsAsExpected(newlyStoredRequests, 1)
        val messageHistory = newlyStoredRequests[0].messageHistory
        assertEquals(
            1,
            messageHistory.size,
            "The message history of the stored data request does not have the expected length.",
        )
        assertEquals(
            null,
            messageHistory[0].message,
            "The message in the message history of the stored data request is not null although it should be.",
        )
        assertEquals(
            setOf(emailAddress),
            messageHistory[0].contacts,
            "The contact list in the message history of the stored data request is not as expected.",
        )
    }

    @Test
    fun `post a single data request and check if patching it changes its status accordingly`() {
        val companyId = getIdForUploadedCompanyWithIdentifiers(permId = System.currentTimeMillis().toString())
        val timestampBeforeSingleRequest = retrieveTimeAndWaitOneMillisecond()
        postStandardSingleDataRequest(companyId)
        val dataRequestId = UUID.fromString(
            getNewlyStoredRequestsAfterTimestamp(timestampBeforeSingleRequest)[0].dataRequestId,
        )
        assertStatusForDataRequestId(dataRequestId, RequestStatus.open)
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        patchDataRequestAndAssertNewStatusAndLastModifiedUpdated(dataRequestId, RequestStatus.answered)
        patchDataRequestAndAssertNewStatusAndLastModifiedUpdated(dataRequestId, RequestStatus.closed)
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
        assertTrue(
            responseBody.contains("Dataland does not know the Data request ID $nonExistingDataRequestId"),
        )
    }

    @Test
    fun `patch data request as a reader and assert that this is forbidden`() {
        val companyId = getIdForUploadedCompanyWithIdentifiers(permId = System.currentTimeMillis().toString())
        val timestampBeforeSingleRequest = retrieveTimeAndWaitOneMillisecond()
        postStandardSingleDataRequest(companyId)
        val dataRequestId = UUID.fromString(
            getNewlyStoredRequestsAfterTimestamp(timestampBeforeSingleRequest)[0].dataRequestId,
        )
        assertStatusForDataRequestId(dataRequestId, RequestStatus.open)
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        val clientException = assertThrows<ClientException> {
            requestControllerApi.patchDataRequestStatus(dataRequestId, RequestStatus.answered)
        }
        assertEquals("Client error : 403 ", clientException.message)
    }

    @Test
    fun `query the data requests as an uploader and assert that it is forbidden`() {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)

        val clientException = assertThrows<ClientException> {
            requestControllerApi.getDataRequests()
        }
        assertEquals("Client error : 403 ", clientException.message)
    }
}
