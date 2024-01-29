package org.dataland.e2etests.tests.communityManager

import org.dataland.communitymanager.openApiClient.api.RequestControllerApi
import org.dataland.communitymanager.openApiClient.infrastructure.ClientError
import org.dataland.communitymanager.openApiClient.infrastructure.ClientException
import org.dataland.communitymanager.openApiClient.model.DataRequestCompanyIdentifierType
import org.dataland.communitymanager.openApiClient.model.RequestStatus
import org.dataland.communitymanager.openApiClient.model.SingleDataRequest
import org.dataland.communitymanager.openApiClient.model.StoredDataRequest
import org.dataland.e2etests.BASE_PATH_TO_COMMUNITY_MANAGER
import org.dataland.e2etests.UPLOADER_USER_ID
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.assertStatusForDataRequestId
import org.dataland.e2etests.utils.check400ClientExceptionErrorMessage
import org.dataland.e2etests.utils.generateRandomIsin
import org.dataland.e2etests.utils.generateRandomPermId
import org.dataland.e2etests.utils.patchDataRequestAndAssertNewStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SingleDataRequestsTest {

    val apiAccessor = ApiAccessor()
    val jwtHelper = JwtAuthenticationHelper()
    private val requestControllerApi = RequestControllerApi(BASE_PATH_TO_COMMUNITY_MANAGER)

    @BeforeAll
    fun authenticateAsReader() { jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader) }

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
        assertEquals(singleDataRequest.listOfReportingPeriods.size, allStoredDataRequests.size)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        for (storedDataRequest in allStoredDataRequests) {
            val retrievedDataRequest = requestControllerApi.getDataRequestById(
                UUID.fromString(storedDataRequest.dataRequestId),
            )
            assertEquals(storedDataRequest, retrievedDataRequest)
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
        check400ClientExceptionErrorMessage(clientException)
        val responseBody = (clientException.response as ClientError<*>).body as String
        assertTrue(responseBody.contains("The provided company identifier has an invalid format."))
        assertTrue(
            responseBody.contains(
                "The company identifier you provided does not match the patterns of a valid LEI, ISIN, PermId or " +
                    "Dataland CompanyID.",
            ),
        )
    }

    @Test
    fun `post a single data request with a valid dataland-company-ID and assure that it is stored as expected`() {
        val companyIdOfNewCompany =
            apiAccessor.uploadOneCompanyWithIdentifiers(permId = generateRandomPermId())!!.actualStoredCompany.companyId
        val singleDataRequest = SingleDataRequest(
            companyIdentifier = companyIdOfNewCompany,
            frameworkName = SingleDataRequest.FrameworkName.lksg,
            listOfReportingPeriods = listOf("2022"),
        )

        val storedDataRequest = requestControllerApi.postSingleDataRequest(singleDataRequest).first()
        val storedDataRequestId = UUID.fromString(storedDataRequest.dataRequestId)

        val retrievedDataRequest = requestControllerApi.getDataRequestById(storedDataRequestId)

        assertEquals(
            DataRequestCompanyIdentifierType.datalandCompanyId,
            retrievedDataRequest.dataRequestCompanyIdentifierType,
        )
        assertEquals(companyIdOfNewCompany, retrievedDataRequest.dataRequestCompanyIdentifierValue)
        assertEquals(RequestStatus.open, retrievedDataRequest.requestStatus)
    }

    @Test
    fun `post a single data request with a PermId that matches a Dataland-company and assert the correct matching`() {
        val validPermId = System.currentTimeMillis().toString()
        val companyIdOfNewCompany =
            apiAccessor.uploadOneCompanyWithIdentifiers(permId = validPermId)!!.actualStoredCompany.companyId
        val singleDataRequest = SingleDataRequest(
            companyIdentifier = validPermId,
            frameworkName = SingleDataRequest.FrameworkName.sfdr,
            listOfReportingPeriods = listOf("2022"),
        )
        val storedDataRequest = requestControllerApi.postSingleDataRequest(singleDataRequest).first()
        val storedDataRequestId = UUID.fromString(storedDataRequest.dataRequestId)

        val retrievedDataRequest = requestControllerApi.getDataRequestById(storedDataRequestId)

        assertEquals(
            DataRequestCompanyIdentifierType.datalandCompanyId,
            retrievedDataRequest.dataRequestCompanyIdentifierType,
        )
        assertEquals(companyIdOfNewCompany, retrievedDataRequest.dataRequestCompanyIdentifierValue)
        assertEquals(RequestStatus.open, retrievedDataRequest.requestStatus)
    }

    // TODO test mit passendem Format der companyId, aber es gibt sie nicht => resource-not-found 404

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
        assertEquals(RequestStatus.open, storedDataRequest.requestStatus)

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

        assertEquals("Client error : 404 ", clientException.message)
        assertTrue(
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
        assertEquals(RequestStatus.open, storedDataRequest.requestStatus)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)

        val clientException = assertThrows<ClientException> {
            requestControllerApi.patchDataRequest(storedDataRequestId, RequestStatus.resolved)
        }
        assertEquals("Client error : 403 ", clientException.message)
    }

    @Test
    fun `query data requests with various filters and assert that the expected results are being retrieved`() {
        val requestA = SingleDataRequest(
            companyIdentifier = generateRandomIsin(),
            frameworkName = SingleDataRequest.FrameworkName.lksg,
            listOfReportingPeriods = listOf("2022"),
        )
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        requestControllerApi.postSingleDataRequest(requestA)

        val permIdSubstring = System.currentTimeMillis().toString()
        val specificPermId = "123456789" + permIdSubstring + "987654321"
        val requestB = SingleDataRequest(
            companyIdentifier = specificPermId,
            frameworkName = SingleDataRequest.FrameworkName.sfdr,
            listOfReportingPeriods = listOf("2021"),
        )
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val req2 = requestControllerApi.postSingleDataRequest(requestB).first()
        requestControllerApi.patchDataRequest(UUID.fromString(req2.dataRequestId), RequestStatus.resolved)

        val allDataRequests = requestControllerApi.getDataRequests()
        val lksgDataRequests = requestControllerApi.getDataRequests(
            dataType = RequestControllerApi.DataTypeGetDataRequests.lksg,
        )
        val reportingPeriod2021DataRequests = requestControllerApi.getDataRequests(reportingPeriod = "2021")
        val resolvedDataRequests = requestControllerApi.getDataRequests(requestStatus = RequestStatus.resolved)
        val specificPermIdDataRequests = requestControllerApi.getDataRequests(
            dataRequestCompanyIdentifierValue = permIdSubstring,
        )
        val specificUsersDataRequests = requestControllerApi.getDataRequests(userId = UPLOADER_USER_ID)

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
        assertTrue(resolvedDataRequests.all { it.requestStatus == RequestStatus.resolved })
        assertTrue(specificPermIdDataRequests.all { it.dataRequestCompanyIdentifierValue.contains(permIdSubstring) })
        assertTrue(specificUsersDataRequests.all { it.userId == UPLOADER_USER_ID })
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
