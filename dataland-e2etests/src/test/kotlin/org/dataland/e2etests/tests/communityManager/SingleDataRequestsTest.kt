package org.dataland.e2etests.tests.communityManager

import org.dataland.communitymanager.openApiClient.api.RequestControllerApi
import org.dataland.communitymanager.openApiClient.infrastructure.ClientError
import org.dataland.communitymanager.openApiClient.infrastructure.ClientException
import org.dataland.communitymanager.openApiClient.model.RequestStatus
import org.dataland.communitymanager.openApiClient.model.SingleDataRequest
import org.dataland.communitymanager.openApiClient.model.StoredDataRequest
import org.dataland.datalandbackend.openApiClient.model.IdentifierType
import org.dataland.e2etests.BASE_PATH_TO_COMMUNITY_MANAGER
import org.dataland.e2etests.PREMIUM_USER_ID
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.assertStatusForDataRequestId
import org.dataland.e2etests.utils.check400ClientExceptionErrorMessage
import org.dataland.e2etests.utils.generateCompaniesWithOneRandomValueForEachIdentifierType
import org.dataland.e2etests.utils.generateRandomIsin
import org.dataland.e2etests.utils.generateRandomLei
import org.dataland.e2etests.utils.generateRandomPermId
import org.dataland.e2etests.utils.getIdForUploadedCompanyWithIdentifiers
import org.dataland.e2etests.utils.getUniqueDatalandCompanyIdForIdentifierValue
import org.dataland.e2etests.utils.patchDataRequestAndAssertNewStatusAndLastModifiedUpdated
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
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
    fun `post single data request and check if retrieval of stored requests via their IDs works as expected`() {
        val stringThatMatchesThePermIdRegex = System.currentTimeMillis().toString()
        generateCompaniesWithOneRandomValueForEachIdentifierType(
            mapOf(IdentifierType.permId to stringThatMatchesThePermIdRegex),
        )
        val singleDataRequest = SingleDataRequest(
            companyIdentifier = stringThatMatchesThePermIdRegex,
            dataType = SingleDataRequest.DataType.lksg,
            reportingPeriods = setOf("2022", "2023"),
            contacts = setOf("someContact@webserver.de", "simpleString@some.thing"),
            message = "This is a test. The current timestamp is ${System.currentTimeMillis()}",
        )
        authenticateAsPremiumUser()
        val allStoredDataRequests = requestControllerApi.postSingleDataRequest(singleDataRequest)
        assertEquals(singleDataRequest.reportingPeriods.size, allStoredDataRequests.size)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        for (storedDataRequest in allStoredDataRequests) {
            val retrievedDataRequest = requestControllerApi.getDataRequestById(
                UUID.fromString(storedDataRequest.dataRequestId),
            )
            assertEquals(storedDataRequest, retrievedDataRequest)
        }
    }

    @Test
    fun `post single data request for companyId with invalid format and assert exception`() {
        val invalidCompanyIdentifier = "a"
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
        val storedDataRequest = requestControllerApi.postSingleDataRequest(singleDataRequest).first()
        val storedDataRequestId = UUID.fromString(storedDataRequest.dataRequestId)

        val retrievedDataRequest = requestControllerApi.getDataRequestById(storedDataRequestId)

        assertEquals(companyIdOfNewCompany, retrievedDataRequest.datalandCompanyId)
        assertEquals(RequestStatus.open, retrievedDataRequest.requestStatus)
    }

    @Test
    fun `post a single data request with a PermId that matches a Dataland company and assert the correct matching`() {
        val validPermId = System.currentTimeMillis().toString()
        val companyIdOfNewCompany = getIdForUploadedCompanyWithIdentifiers(permId = validPermId)
        val storedDataRequest = postStandardSingleDataRequest(validPermId)
        val storedDataRequestId = UUID.fromString(storedDataRequest.dataRequestId)

        val retrievedDataRequest = requestControllerApi.getDataRequestById(storedDataRequestId)

        assertEquals(companyIdOfNewCompany, retrievedDataRequest.datalandCompanyId)
        assertEquals(RequestStatus.open, retrievedDataRequest.requestStatus)
    }

    private fun postStandardSingleDataRequest(
        companyIdentifier: String,
        contacts: Set<String>? = null,
        message: String? = null,
    ): StoredDataRequest {
        return requestControllerApi.postSingleDataRequest(
            SingleDataRequest(
                companyIdentifier = companyIdentifier,
                dataType = SingleDataRequest.DataType.sfdr,
                reportingPeriods = setOf("2022"),
                contacts = contacts,
                message = message,
            ),
        ).first()
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
        val validLei = generateRandomLei()
        apiAccessor.uploadOneCompanyWithIdentifiers(lei = validLei)
        val storedDataRequest = postStandardSingleDataRequest(validLei, setOf("test@someprovider.abc"))
        val storedDataRequestId = UUID.fromString(storedDataRequest.dataRequestId)
        val retrievedDataRequest = requestControllerApi.getDataRequestById(storedDataRequestId)
        assertEquals(
            storedDataRequest,
            retrievedDataRequest,
        )
    }

    @Test
    fun `post a single data request and check if patching it changes its status accordingly`() {
        val stringThatMatchesThePermIdRegex = System.currentTimeMillis().toString()
        generateCompaniesWithOneRandomValueForEachIdentifierType(
            mapOf(IdentifierType.permId to stringThatMatchesThePermIdRegex),
        )
        val singleDataRequest = SingleDataRequest(
            companyIdentifier = stringThatMatchesThePermIdRegex,
            dataType = SingleDataRequest.DataType.lksg,
            reportingPeriods = setOf("2022"),
        )
        authenticateAsPremiumUser()
        val storedDataRequest = requestControllerApi.postSingleDataRequest(singleDataRequest).first()
        val storedDataRequestId = UUID.fromString(storedDataRequest.dataRequestId)
        assertEquals(RequestStatus.open, storedDataRequest.requestStatus)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)

        assertStatusForDataRequestId(storedDataRequestId, RequestStatus.open)

        patchDataRequestAndAssertNewStatusAndLastModifiedUpdated(storedDataRequestId, RequestStatus.answered)

        patchDataRequestAndAssertNewStatusAndLastModifiedUpdated(storedDataRequestId, RequestStatus.open)
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
    fun `patch data request as an reader and assert that it is forbidden`() {
        val stringThatMatchesThePermIdRegex = System.currentTimeMillis().toString()
        generateCompaniesWithOneRandomValueForEachIdentifierType(
            mapOf(IdentifierType.permId to stringThatMatchesThePermIdRegex),
        )
        val singleDataRequest = SingleDataRequest(
            companyIdentifier = stringThatMatchesThePermIdRegex,
            dataType = SingleDataRequest.DataType.lksg,
            reportingPeriods = setOf("2022"),
        )
        authenticateAsPremiumUser()
        val storedDataRequest = requestControllerApi.postSingleDataRequest(singleDataRequest).first()
        val storedDataRequestId = UUID.fromString(storedDataRequest.dataRequestId)
        assertEquals(RequestStatus.open, storedDataRequest.requestStatus)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)

        val clientException = assertThrows<ClientException> {
            requestControllerApi.patchDataRequestStatus(storedDataRequestId, RequestStatus.answered)
        }
        assertEquals("Client error : 403 ", clientException.message)
    }

    private fun postDataRequestsBeforeQueryTest(): List<SingleDataRequest> {
        val isinString = generateRandomIsin()
        generateCompaniesWithOneRandomValueForEachIdentifierType(
            mapOf(IdentifierType.isin to isinString),
        )
        val requestA = SingleDataRequest(
            companyIdentifier = isinString,
            dataType = SingleDataRequest.DataType.lksg,
            reportingPeriods = setOf("2022"),
        )
        authenticateAsPremiumUser()
        requestControllerApi.postSingleDataRequest(requestA)

        val specificPermId = System.currentTimeMillis().toString()
        generateCompaniesWithOneRandomValueForEachIdentifierType(
            mapOf(IdentifierType.permId to specificPermId),
        )

        val requestB = SingleDataRequest(
            companyIdentifier = specificPermId,
            dataType = SingleDataRequest.DataType.sfdr,
            reportingPeriods = setOf("2021"),
        )
        authenticateAsPremiumUser()
        val req2 = requestControllerApi.postSingleDataRequest(requestB).first()
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        requestControllerApi.patchDataRequestStatus(UUID.fromString(req2.dataRequestId), RequestStatus.answered)
        return listOf(requestA, requestB)
    }

    @Test
    fun `query data requests with various filters and assert that the expected results are being retrieved`() {
        val singleDataRequests = postDataRequestsBeforeQueryTest()
        val permIdOfRequestB = singleDataRequests[1].companyIdentifier
        val companyIdForPermId = getUniqueDatalandCompanyIdForIdentifierValue(permIdOfRequestB)

        val allDataRequests = requestControllerApi.getDataRequests()
        val lksgDataRequests = requestControllerApi.getDataRequests(
            dataType = RequestControllerApi.DataTypeGetDataRequests.lksg,
        )
        val reportingPeriod2021DataRequests = requestControllerApi.getDataRequests(reportingPeriod = "2021")
        val resolvedDataRequests = requestControllerApi.getDataRequests(requestStatus = RequestStatus.answered)
        val specificPermIdDataRequests = requestControllerApi.getDataRequests(
            datalandCompanyId = getUniqueDatalandCompanyIdForIdentifierValue(permIdOfRequestB),
        )

        val specificUsersDataRequests = requestControllerApi.getDataRequests(userId = PREMIUM_USER_ID)

        val allQueryResults = listOf(
            allDataRequests, lksgDataRequests, reportingPeriod2021DataRequests,
            resolvedDataRequests, specificPermIdDataRequests, specificUsersDataRequests,
        )
        allQueryResults.forEach { storedDataRequestsQueryResult ->
            assertTrue(storedDataRequestsQueryResult.isNotEmpty())
        }

        assertTrue(allDataRequests.size > 1)
        assertTrue(lksgDataRequests.all { it.dataType == StoredDataRequest.DataType.lksg })
        assertTrue(reportingPeriod2021DataRequests.all { it.reportingPeriod == "2021" })
        assertTrue(
            specificPermIdDataRequests.all {
                it.datalandCompanyId == getUniqueDatalandCompanyIdForIdentifierValue(permIdOfRequestB)
            },
        )
        assertTrue(resolvedDataRequests.all { it.requestStatus == RequestStatus.answered })
        assertTrue(specificPermIdDataRequests.all { it.datalandCompanyId == companyIdForPermId })
        assertTrue(specificUsersDataRequests.all { it.userId == PREMIUM_USER_ID })
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
