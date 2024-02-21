package org.dataland.e2etests.tests.communityManager

import org.dataland.communitymanager.openApiClient.api.RequestControllerApi
import org.dataland.communitymanager.openApiClient.model.BulkDataRequest
import org.dataland.communitymanager.openApiClient.model.DataRequestCompanyIdentifierType
import org.dataland.communitymanager.openApiClient.model.StoredDataRequest
import org.dataland.e2etests.BASE_PATH_TO_COMMUNITY_MANAGER
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.causeClientExceptionByBulkDataRequest
import org.dataland.e2etests.utils.checkErrorMessageForInvalidIdentifiersInBulkRequest
import org.dataland.e2etests.utils.checkThatAllIdentifiersWereAccepted
import org.dataland.e2etests.utils.checkThatMessageIsAsExpected
import org.dataland.e2etests.utils.checkThatRequestForFrameworkReportingPeriodAndIdentifierExistsExactlyOnce
import org.dataland.e2etests.utils.checkThatTheAmountOfNewlyStoredRequestsIsAsExpected
import org.dataland.e2etests.utils.checkThatTheNumberOfAcceptedIdentifiersIsAsExpected
import org.dataland.e2etests.utils.checkThatTheNumberOfRejectedIdentifiersIsAsExpected
import org.dataland.e2etests.utils.generateMapWithOneRandomValueForEachIdentifierType
import org.dataland.e2etests.utils.generateRandomIsin
import org.dataland.e2etests.utils.generateRandomLei
import org.dataland.e2etests.utils.generateRandomPermId
import org.dataland.e2etests.utils.getIdForUploadedCompanyWithIdentifiers
import org.dataland.e2etests.utils.retrieveTimeAndWaitOneMillisecond
import org.dataland.e2etests.utils.sendBulkRequestWithEmptyInputAndCheckErrorMessage
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BulkDataRequestsTest {

    val jwtHelper = JwtAuthenticationHelper()
    private val requestControllerApi = RequestControllerApi(BASE_PATH_TO_COMMUNITY_MANAGER)

    private fun getNewlyStoredRequestsAfterTimestamp(timestamp: Long): List<StoredDataRequest> {
        return requestControllerApi.getDataRequestsForUser().filter { storedDataRequest ->
            storedDataRequest.creationTimestamp > timestamp
        }
    }

    @BeforeAll
    fun authenticateAsReader() { jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader) }

    @Test
    fun `post bulk data request for all frameworks and different valid identifiers and check stored requests`() {
        val uniqueIdentifiersMap = generateMapWithOneRandomValueForEachIdentifierType()
        val multipleRegexMatchingIdentifier = generateRandomPermId(20)
        val identifiers = uniqueIdentifiersMap.values.toList() + listOf(multipleRegexMatchingIdentifier)
        val frameworks = enumValues<BulkDataRequest.ListOfFrameworkNames>().toList()
        val reportingPeriods = listOf("2022", "2023")
        val timestampBeforeBulkRequest = retrieveTimeAndWaitOneMillisecond()
        val response = requestControllerApi.postBulkDataRequest(
            BulkDataRequest(identifiers, frameworks, reportingPeriods),
        )
        checkThatAllIdentifiersWereAccepted(response, identifiers.size)
        val newlyStoredRequests = getNewlyStoredRequestsAfterTimestamp(timestampBeforeBulkRequest)
        checkThatTheAmountOfNewlyStoredRequestsIsAsExpected(
            newlyStoredRequests, identifiers.size * frameworks.size * reportingPeriods.size,
        )
        val randomUniqueDataRequestCompanyIdentifierType = uniqueIdentifiersMap.keys.random()
        uniqueIdentifiersMap[randomUniqueDataRequestCompanyIdentifierType]?.let {
            checkThatRequestForFrameworkReportingPeriodAndIdentifierExistsExactlyOnce(
                newlyStoredRequests, frameworks.random(), reportingPeriods.random(),
                randomUniqueDataRequestCompanyIdentifierType, it,
            )
        }
        checkThatRequestForFrameworkReportingPeriodAndIdentifierExistsExactlyOnce(
            newlyStoredRequests, frameworks.random(), reportingPeriods.random(),
            DataRequestCompanyIdentifierType.multipleRegexMatches, multipleRegexMatchingIdentifier,
        )
    }

    @Test
    fun `post a bulk data request with at least one invalid identifier and check that this gives no stored request`() {
        val validIdentifiers = listOf(
            generateRandomLei(), generateRandomIsin(), generateRandomPermId(),
        )
        val invalidIdentifiers = listOf(
            generateRandomLei() + "F", generateRandomIsin() + "F", generateRandomPermId() + "F",
        )
        val timestampBeforeBulkRequest = retrieveTimeAndWaitOneMillisecond()
        val response = requestControllerApi.postBulkDataRequest(
            BulkDataRequest(
                validIdentifiers + invalidIdentifiers,
                listOf(BulkDataRequest.ListOfFrameworkNames.lksg),
                listOf("2023"),
            ),
        )
        checkThatTheNumberOfAcceptedIdentifiersIsAsExpected(response, validIdentifiers.size)
        checkThatTheNumberOfRejectedIdentifiersIsAsExpected(response, invalidIdentifiers.size)
        checkThatMessageIsAsExpected(response, validIdentifiers.size, invalidIdentifiers.size)
        val newlyStoredRequests = getNewlyStoredRequestsAfterTimestamp(timestampBeforeBulkRequest)
        checkThatTheAmountOfNewlyStoredRequestsIsAsExpected(newlyStoredRequests, validIdentifiers.size)
        assertFalse(
            newlyStoredRequests.any { invalidIdentifiers.contains(it.dataRequestCompanyIdentifierValue) },
        )
    }

    @Test
    fun `post bulk data request with at least one company duplicate and check that only one request is stored`() {
        val leiForCompany = generateRandomLei()
        val isinForCompany = generateRandomIsin()
        val companyId = getIdForUploadedCompanyWithIdentifiers(leiForCompany, listOf(isinForCompany))
        val identifierTypeForUnknownCompany = DataRequestCompanyIdentifierType.lei
        val identifierValueForUnknownCompany = generateRandomLei()
        val identifiersForBulkRequest = listOf(
            leiForCompany, isinForCompany, identifierValueForUnknownCompany,
        )
        val frameworksForBulkRequest = listOf(BulkDataRequest.ListOfFrameworkNames.lksg)
        val reportingPeriodsForBulkRequest = listOf("2023")
        val timestampBeforeBulkRequest = retrieveTimeAndWaitOneMillisecond()
        val response = requestControllerApi.postBulkDataRequest(
            BulkDataRequest(identifiersForBulkRequest, frameworksForBulkRequest, reportingPeriodsForBulkRequest),
        )
        checkThatAllIdentifiersWereAccepted(response, identifiersForBulkRequest.size)
        val newlyStoredRequests = getNewlyStoredRequestsAfterTimestamp(timestampBeforeBulkRequest)
        checkThatTheAmountOfNewlyStoredRequestsIsAsExpected(
            newlyStoredRequests,
            (identifiersForBulkRequest.size - 1) * frameworksForBulkRequest.size * reportingPeriodsForBulkRequest.size,
        )
        checkThatRequestForFrameworkReportingPeriodAndIdentifierExistsExactlyOnce(
            newlyStoredRequests, frameworksForBulkRequest[0], reportingPeriodsForBulkRequest[0],
            DataRequestCompanyIdentifierType.datalandCompanyId, companyId,
        )
        checkThatRequestForFrameworkReportingPeriodAndIdentifierExistsExactlyOnce(
            newlyStoredRequests, frameworksForBulkRequest[0], reportingPeriodsForBulkRequest[0],
            identifierTypeForUnknownCompany, identifierValueForUnknownCompany,
        )
    }

    private fun checkThatBothRequestsExistExactlyOnceAfterBulkRequest(
        requestsStoredAfterBulkRequest: List<StoredDataRequest>,
        framework: BulkDataRequest.ListOfFrameworkNames,
        reportingPeriod: String,
        companyId: String,
        identifierTypeForUnknownCompany: DataRequestCompanyIdentifierType,
        identifierValueForUnknownCompany: String,
    ) {
        checkThatRequestForFrameworkReportingPeriodAndIdentifierExistsExactlyOnce(
            requestsStoredAfterBulkRequest,
            framework,
            reportingPeriod,
            DataRequestCompanyIdentifierType.datalandCompanyId,
            companyId,
        )
        checkThatRequestForFrameworkReportingPeriodAndIdentifierExistsExactlyOnce(
            requestsStoredAfterBulkRequest,
            framework,
            reportingPeriod,
            identifierTypeForUnknownCompany,
            identifierValueForUnknownCompany,
        )
    }

    private fun checkThatAlreadyExistingRequestsAreNeitherStoredForKnownNorForUnknownCompanies(
        frameworks: List<BulkDataRequest.ListOfFrameworkNames>,
        reportingPeriods: List<String>,
        companyId: String,
        identifierMapForUnknownCompany: Map<DataRequestCompanyIdentifierType, String>,
        firstIdentifiers: List<String>,
        secondIdentifiers: List<String>,
    ) {
        val timeBeforeFirstBulkRequest = retrieveTimeAndWaitOneMillisecond()
        val firstResponse = requestControllerApi.postBulkDataRequest(
            BulkDataRequest(firstIdentifiers, frameworks, reportingPeriods),
        )
        checkThatAllIdentifiersWereAccepted(firstResponse, firstIdentifiers.size)
        val newRequestsAfter1stBulkRequest = getNewlyStoredRequestsAfterTimestamp(timeBeforeFirstBulkRequest)
        checkThatTheAmountOfNewlyStoredRequestsIsAsExpected(
            newRequestsAfter1stBulkRequest, firstIdentifiers.size * frameworks.size * reportingPeriods.size,
        )
        checkThatBothRequestsExistExactlyOnceAfterBulkRequest(
            newRequestsAfter1stBulkRequest, frameworks[0], reportingPeriods[0], companyId,
            identifierMapForUnknownCompany.keys.toList()[0], identifierMapForUnknownCompany.values.toList()[0],
        )
        val timestampBeforeSecondBulkRequest = retrieveTimeAndWaitOneMillisecond()
        val secondResponse = requestControllerApi.postBulkDataRequest(
            BulkDataRequest(secondIdentifiers, frameworks, reportingPeriods),
        )
        checkThatAllIdentifiersWereAccepted(secondResponse, secondIdentifiers.size)
        val newRequestsAfter2ndBulkRequest = getNewlyStoredRequestsAfterTimestamp(timestampBeforeSecondBulkRequest)
        checkThatTheAmountOfNewlyStoredRequestsIsAsExpected(newRequestsAfter2ndBulkRequest, 0)
        val newRequestsAfter1stAnd2ndBulkRequest = getNewlyStoredRequestsAfterTimestamp(timeBeforeFirstBulkRequest)
        checkThatBothRequestsExistExactlyOnceAfterBulkRequest(
            newRequestsAfter1stAnd2ndBulkRequest, frameworks[0], reportingPeriods[0], companyId,
            identifierMapForUnknownCompany.keys.toList()[0], identifierMapForUnknownCompany.values.toList()[0],
        )
    }

    @Test
    fun `post a bulk data request with at least one already existing request and check that this one is ignored`() {
        val leiForCompany = generateRandomLei()
        val isinForCompany = generateRandomIsin()
        val companyId = getIdForUploadedCompanyWithIdentifiers(leiForCompany, listOf(isinForCompany))
        val identifierMapForUnknownCompany = mapOf(DataRequestCompanyIdentifierType.lei to generateRandomLei())
        val frameworks = listOf(BulkDataRequest.ListOfFrameworkNames.lksg)
        val reportingPeriods = listOf("2023")
        val firstIdentifiers = listOf(leiForCompany, identifierMapForUnknownCompany.values.toList()[0])
        val secondIdentifiers = listOf(isinForCompany, identifierMapForUnknownCompany.values.toList()[0])
        checkThatAlreadyExistingRequestsAreNeitherStoredForKnownNorForUnknownCompanies(
            frameworks,
            reportingPeriods,
            companyId,
            identifierMapForUnknownCompany,
            firstIdentifiers,
            secondIdentifiers,
        )
    }

    @Test
    fun `check the expected exception is thrown when frameworks are empty or identifiers are empty or invalid only`() {
        val validIdentifiers = listOf(generateRandomLei(), generateRandomIsin(), generateRandomPermId())
        val frameworks = enumValues<BulkDataRequest.ListOfFrameworkNames>().toList()
        val reportingPeriods = listOf("2023")
        sendBulkRequestWithEmptyInputAndCheckErrorMessage(validIdentifiers, frameworks, emptyList())
        sendBulkRequestWithEmptyInputAndCheckErrorMessage(validIdentifiers, emptyList(), reportingPeriods)
        sendBulkRequestWithEmptyInputAndCheckErrorMessage(emptyList(), frameworks, reportingPeriods)
        sendBulkRequestWithEmptyInputAndCheckErrorMessage(validIdentifiers, emptyList(), emptyList())
        sendBulkRequestWithEmptyInputAndCheckErrorMessage(emptyList(), frameworks, emptyList())
        sendBulkRequestWithEmptyInputAndCheckErrorMessage(emptyList(), emptyList(), reportingPeriods)
        sendBulkRequestWithEmptyInputAndCheckErrorMessage(emptyList(), emptyList(), emptyList())
        val invalidIdentifiers = listOf(
            generateRandomLei() + "F", generateRandomIsin() + "F", generateRandomPermId() + "F",
        )
        val clientException = causeClientExceptionByBulkDataRequest(invalidIdentifiers, frameworks, reportingPeriods)
        checkErrorMessageForInvalidIdentifiersInBulkRequest(clientException)
    }
}
