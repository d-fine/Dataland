package org.dataland.e2etests.tests.communityManager

import org.dataland.communitymanager.openApiClient.api.RequestControllerApi
import org.dataland.communitymanager.openApiClient.infrastructure.ClientError
import org.dataland.communitymanager.openApiClient.infrastructure.ClientException
import org.dataland.communitymanager.openApiClient.model.CompanyRole
import org.dataland.communitymanager.openApiClient.model.RequestStatus
import org.dataland.communitymanager.openApiClient.model.SingleDataRequest
import org.dataland.e2etests.BASE_PATH_TO_COMMUNITY_MANAGER
import org.dataland.e2etests.auth.GlobalAuth.withTechnicalUser
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.communityManager.checkClientExceptionErrorMessage
import org.dataland.e2etests.utils.communityManager.checkThatAllReportingPeriodsAreTreatedAsExpected
import org.dataland.e2etests.utils.communityManager.checkThatDataRequestExistsExactlyOnceInRecentlyStored
import org.dataland.e2etests.utils.communityManager.checkThatTheAmountOfNewlyStoredRequestsIsAsExpected
import org.dataland.e2etests.utils.communityManager.generateRandomLei
import org.dataland.e2etests.utils.communityManager.generateRandomPermId
import org.dataland.e2etests.utils.communityManager.getIdForUploadedCompanyWithIdentifiers
import org.dataland.e2etests.utils.communityManager.getMessageHistoryOfRequest
import org.dataland.e2etests.utils.communityManager.getNewlyStoredRequestsAfterTimestamp
import org.dataland.e2etests.utils.communityManager.postSingleDataRequestForReportingPeriodAndUpdateStatus
import org.dataland.e2etests.utils.communityManager.postStandardSingleDataRequest
import org.dataland.e2etests.utils.communityManager.retrieveTimeAndWaitOneMillisecond
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SingleDataRequestsTest {
    val apiAccessor = ApiAccessor()
    val jwtHelper = JwtAuthenticationHelper()
    private val requestControllerApi = RequestControllerApi(BASE_PATH_TO_COMMUNITY_MANAGER)
    private val maxRequestsForUser = 10
    private val dataReaderUserId = UUID.fromString(TechnicalUser.Reader.technicalUserId)
    private val clientErrorMessage403 = "Client error : 403 "
    private val companyNotFoundExceptionSummary = "The company identifier is unknown."

    private fun companyNotFoundExceptionMessage(companyIdentifier: String) =
        "No company is associated to the identifier $companyIdentifier."

    @BeforeEach
    fun authenticateAsPremiumUser() {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.PremiumUser)
    }

    @Test
    fun `post single data request for multiple reporting periods and check stored requests`() {
        val stringThatMatchesThePermIdRegex = System.currentTimeMillis().toString()
        val companyId = getIdForUploadedCompanyWithIdentifiers(permId = stringThatMatchesThePermIdRegex)
        val reportingPeriods = setOf("2022", "2023")
        val singleDataRequest =
            SingleDataRequest(
                companyIdentifier = stringThatMatchesThePermIdRegex,
                dataType = SingleDataRequest.DataType.lksg,
                reportingPeriods = reportingPeriods,
                contacts = setOf("someContact@example.com", "simpleString@example.com"),
                message = "This is a test. The current timestamp is ${System.currentTimeMillis()}",
                notifyMeImmediately = false,
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
            assertEquals(companyId, it.datalandCompanyId, "The company ID in a stored data request is not as expected.")
        }
        reportingPeriods.forEach {
            checkThatDataRequestExistsExactlyOnceInRecentlyStored(
                newlyStoredRequests, SingleDataRequest.DataType.lksg.value, it, companyId,
            )
        }
    }

    @Test
    fun `post single data request for companyId with invalid format and assert exception`() {
        val invalidCompanyIdentifier = "invalid-identifier-${Instant.now().toEpochMilli()}"
        val invalidSingleDataRequest =
            SingleDataRequest(
                companyIdentifier = invalidCompanyIdentifier,
                dataType = SingleDataRequest.DataType.lksg,
                reportingPeriods = setOf("2022"),
                notifyMeImmediately = false,
            )
        val clientException =
            assertThrows<ClientException> {
                requestControllerApi.postSingleDataRequest(invalidSingleDataRequest)
            }
        checkClientExceptionErrorMessage(clientException, 404)
        val responseBody = (clientException.response as ClientError<*>).body as String
        assertTrue(responseBody.contains(companyNotFoundExceptionSummary))
        assertTrue(responseBody.contains(companyNotFoundExceptionMessage(invalidCompanyIdentifier)))
    }

    @Test
    fun `post single data request without reporting periods and assert exception`() {
        val companyId = getIdForUploadedCompanyWithIdentifiers(lei = generateRandomLei())
        val clientException =
            assertThrows<ClientException> {
                requestControllerApi.postSingleDataRequest(
                    SingleDataRequest(
                        companyIdentifier = companyId,
                        dataType = SingleDataRequest.DataType.lksg,
                        reportingPeriods = setOf(),
                        notifyMeImmediately = false,
                    ),
                )
            }
        checkClientExceptionErrorMessage(clientException)
        val responseBody = (clientException.response as ClientError<*>).body as String
        assertTrue(responseBody.contains("The list of reporting periods must not be empty."))
        assertTrue(
            responseBody.contains(
                "At least one reporting period must be provided. Without, no meaningful request can be created.",
            ),
        )
    }

    @Test
    fun `post single data request for a companyId which is unknown to Dataland and assert exception`() {
        val unknownCompanyId = UUID.randomUUID().toString()
        val singleDataRequest =
            SingleDataRequest(
                companyIdentifier = unknownCompanyId,
                dataType = SingleDataRequest.DataType.lksg,
                reportingPeriods = setOf("2022"),
                notifyMeImmediately = false,
            )
        val clientException =
            assertThrows<ClientException> {
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
        val singleDataRequest =
            SingleDataRequest(
                companyIdentifier = companyIdOfNewCompany,
                dataType = SingleDataRequest.DataType.lksg,
                reportingPeriods = setOf("2022"),
                notifyMeImmediately = false,
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
            RequestStatus.Open,
            newlyStoredRequests[0].requestStatus,
            "The new data request is not stored with status 'Open'.",
        )
    }

    @Test
    fun `post single data requests with message but invalid email addresses in contact lists and assert exception`() {
        val validLei = generateRandomLei()
        apiAccessor.uploadOneCompanyWithIdentifiers(lei = validLei)

        val contactListsThatContainInvalidEmailAddresses =
            listOf(listOf(""), listOf(" "), listOf("invalidMail@", "validMail@example.com"))
        contactListsThatContainInvalidEmailAddresses.forEach {
            val clientException =
                assertThrows<ClientException> {
                    postStandardSingleDataRequest(validLei, it.toSet(), "Dummy test message.")
                }
            checkClientExceptionErrorMessage(clientException)
            val responseBody = (clientException.response as ClientError<*>).body as String
            assertTrue(responseBody.contains("Invalid contact ${it[0]}"))
            assertTrue(
                responseBody.contains(
                    "The provided contact ${it[0]} is not valid. Please specify a valid email address " +
                        "or when a company owner exists COMPANY_OWNER.",
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
            val clientException =
                assertThrows<ClientException> {
                    postStandardSingleDataRequest(validLei, it, "Dummy test message.")
                }
            checkClientExceptionErrorMessage(clientException)
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
        val emailAddress = "test@example.com"
        val timestampBeforeSingleRequest = retrieveTimeAndWaitOneMillisecond()
        val response = postStandardSingleDataRequest(companyId, setOf(emailAddress))
        checkThatAllReportingPeriodsAreTreatedAsExpected(
            singleDataRequestResponse = response,
            expectedNumberOfStoredReportingPeriods = 1,
            expectedNumberOfDuplicateReportingPeriods = 0,
        )
        val newlyStoredRequests = getNewlyStoredRequestsAfterTimestamp(timestampBeforeSingleRequest)
        checkThatTheAmountOfNewlyStoredRequestsIsAsExpected(newlyStoredRequests, 1)
        val messageHistory = getMessageHistoryOfRequest(newlyStoredRequests[0].dataRequestId)
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
    fun `query the data requests as an uploader and assert that it is forbidden`() {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)

        val clientException =
            assertThrows<ClientException> {
                requestControllerApi.getDataRequests()
            }
        assertEquals(clientErrorMessage403, clientException.message)
    }

    @Test
    fun `post a duplicate request and check that it is only stored if previous in final status`() {
        val companyId = getIdForUploadedCompanyWithIdentifiers(lei = generateRandomLei())
        postSingleDataRequestForReportingPeriodAndUpdateStatus(companyId, "2021")
        postSingleDataRequestForReportingPeriodAndUpdateStatus(companyId, "2022", RequestStatus.Answered)
        postSingleDataRequestForReportingPeriodAndUpdateStatus(companyId, "2023", RequestStatus.Resolved)
        val timestampBeforeFinalRequest = retrieveTimeAndWaitOneMillisecond()
        val response =
            requestControllerApi.postSingleDataRequest(
                SingleDataRequest(
                    companyIdentifier = companyId,
                    dataType = SingleDataRequest.DataType.lksg,
                    reportingPeriods = setOf("2021", "2022", "2023"),
                    notifyMeImmediately = false,
                ),
            )
        checkThatAllReportingPeriodsAreTreatedAsExpected(
            singleDataRequestResponse = response,
            expectedNumberOfStoredReportingPeriods = 1,
            expectedNumberOfDuplicateReportingPeriods = 2,
        )
        val newlyStoredRequests = getNewlyStoredRequestsAfterTimestamp(timestampBeforeFinalRequest)
        checkThatTheAmountOfNewlyStoredRequestsIsAsExpected(newlyStoredRequests, 1)
        assertEquals(
            "2023",
            newlyStoredRequests[0].reportingPeriod,
            "The reporting period of the one newly stored request is not as expected.",
        )
    }

    @Test
    fun `post several single data requests as a company member and check that no quota is applied`() {
        val stringThatMatchesThePermIdRegex = System.currentTimeMillis().toString()
        val companyId = getIdForUploadedCompanyWithIdentifiers(permId = stringThatMatchesThePermIdRegex)
        val reportingPeriods = (2000..(2000 + maxRequestsForUser + 1)).map { it.toString() }.toSet()
        val singleDataRequest =
            SingleDataRequest(
                companyIdentifier = stringThatMatchesThePermIdRegex,
                dataType = SingleDataRequest.DataType.lksg,
                reportingPeriods = reportingPeriods,
                contacts = setOf("someContact@example.com", "simpleString@example.com"),
                message = "This is a test. The current timestamp is ${System.currentTimeMillis()}",
                notifyMeImmediately = false,
            )

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        assertThrows<ClientException> { requestControllerApi.postSingleDataRequest(singleDataRequest) }

        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        apiAccessor.companyRolesControllerApi.assignCompanyRole(
            CompanyRole.Member,
            UUID.fromString(companyId),
            dataReaderUserId,
        )

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        assertDoesNotThrow { requestControllerApi.postSingleDataRequest(singleDataRequest) }
    }

    @Test
    fun `post single data request with too short company identifier and assert correct error response`() {
        val tooShortCompanyIdentifier = "aa"
        val singleDataRequest =
            SingleDataRequest(
                companyIdentifier = tooShortCompanyIdentifier,
                dataType = SingleDataRequest.DataType.lksg,
                reportingPeriods = setOf("2025"),
                contacts = setOf("someMail@example.com"),
                message = "Does not matter for this test.",
                notifyMeImmediately = false,
            )

        withTechnicalUser(TechnicalUser.Reader) {
            val exception =
                assertThrows<ClientException> { requestControllerApi.postSingleDataRequest(singleDataRequest) }
            checkClientExceptionErrorMessage(exception, 404)

            val responseString = (exception.response as ClientError<*>).body as String
            assertTrue(responseString.contains(companyNotFoundExceptionSummary))
            assertTrue(responseString.contains(companyNotFoundExceptionMessage(tooShortCompanyIdentifier)))
        }
    }
}
