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
import org.dataland.e2etests.utils.findAggregatedDataRequestDataTypeForFramework
import org.dataland.e2etests.utils.findRequestControllerApiDataTypeForFramework
import org.dataland.e2etests.utils.generateMapWithOneRandomValueForEachIdentifierType
import org.dataland.e2etests.utils.generateRandomIsin
import org.dataland.e2etests.utils.generateRandomLei
import org.dataland.e2etests.utils.generateRandomPermId
import org.dataland.e2etests.utils.getIdForUploadedCompanyWithIdentifiers
import org.dataland.e2etests.utils.iterateThroughFrameworksReportingPeriodsAndIdentifiersAndCheckAggregationWithCount
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
        val identifiers = uniqueIdentifiersMap.values.toSet() + setOf(multipleRegexMatchingIdentifier)
        val dataTypes = enumValues<BulkDataRequest.DataTypes>().toSet()
        val reportingPeriods = setOf("2022", "2023")
        val timestampBeforeBulkRequest = retrieveTimeAndWaitOneMillisecond()
        val response = requestControllerApi.postBulkDataRequest(
            BulkDataRequest(identifiers, dataTypes, reportingPeriods),
        )
        checkThatAllIdentifiersWereAccepted(response, identifiers.size)
        val newlyStoredRequests = getNewlyStoredRequestsAfterTimestamp(timestampBeforeBulkRequest)
        checkThatTheAmountOfNewlyStoredRequestsIsAsExpected(
            newlyStoredRequests, identifiers.size * dataTypes.size * reportingPeriods.size,
        )
        val randomUniqueDataRequestCompanyIdentifierType = uniqueIdentifiersMap.keys.random()
        uniqueIdentifiersMap[randomUniqueDataRequestCompanyIdentifierType]?.let {
            checkThatRequestForFrameworkReportingPeriodAndIdentifierExistsExactlyOnce(
                newlyStoredRequests, dataTypes.random(), reportingPeriods.random(),
                randomUniqueDataRequestCompanyIdentifierType, it,
            )
        }
        checkThatRequestForFrameworkReportingPeriodAndIdentifierExistsExactlyOnce(
            newlyStoredRequests, dataTypes.random(), reportingPeriods.random(),
            DataRequestCompanyIdentifierType.multipleRegexMatches, multipleRegexMatchingIdentifier,
        )
    }

    @Test
    fun `post a bulk data request with at least one invalid identifier and check that this gives no stored request`() {
        val validIdentifiers = setOf(
            generateRandomLei(), generateRandomIsin(), generateRandomPermId(),
        )
        val invalidIdentifiers = setOf(
            generateRandomLei() + "F", generateRandomIsin() + "F", generateRandomPermId() + "F",
        )
        val timestampBeforeBulkRequest = retrieveTimeAndWaitOneMillisecond()
        val response = requestControllerApi.postBulkDataRequest(
            BulkDataRequest(
                validIdentifiers + invalidIdentifiers,
                setOf(BulkDataRequest.DataTypes.lksg),
                setOf("2023"),
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
        val identifierValueForUnknownCompany = generateRandomLei()
        val identifiersForBulkRequest = setOf(
            leiForCompany, isinForCompany, identifierValueForUnknownCompany,
        )
        val frameworksForBulkRequest = listOf(BulkDataRequest.DataTypes.lksg)
        val reportingPeriod = "2023"
        val timestampBeforeBulkRequest = retrieveTimeAndWaitOneMillisecond()
        val response = requestControllerApi.postBulkDataRequest(
            BulkDataRequest(identifiersForBulkRequest, frameworksForBulkRequest.toSet(), setOf(reportingPeriod)),
        )
        checkThatAllIdentifiersWereAccepted(response, identifiersForBulkRequest.size)
        val newlyStoredRequests = getNewlyStoredRequestsAfterTimestamp(timestampBeforeBulkRequest)
        checkThatTheAmountOfNewlyStoredRequestsIsAsExpected(
            newlyStoredRequests,
            (identifiersForBulkRequest.size - 1) * frameworksForBulkRequest.size,
        )
        checkThatRequestForFrameworkReportingPeriodAndIdentifierExistsExactlyOnce(
            newlyStoredRequests, frameworksForBulkRequest[0], reportingPeriod,
            DataRequestCompanyIdentifierType.datalandCompanyId, companyId,
        )
        checkThatRequestForFrameworkReportingPeriodAndIdentifierExistsExactlyOnce(
            newlyStoredRequests, frameworksForBulkRequest[0], reportingPeriod,
            DataRequestCompanyIdentifierType.lei, identifierValueForUnknownCompany,
        )
    }

    private fun checkThatBothRequestsExistExactlyOnceAfterBulkRequest(
        requestsStoredAfterBulkRequest: List<StoredDataRequest>,
        framework: BulkDataRequest.DataTypes,
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
        dataTypes: List<BulkDataRequest.DataTypes>,
        reportingPeriods: List<String>,
        companyId: String,
        identifierMapForUnknownCompany: Map<DataRequestCompanyIdentifierType, String>,
        firstIdentifiers: Set<String>,
        secondIdentifiers: Set<String>,
    ) {
        val timeBeforeFirstBulkRequest = retrieveTimeAndWaitOneMillisecond()
        val firstResponse = requestControllerApi.postBulkDataRequest(
            BulkDataRequest(firstIdentifiers, dataTypes.toSet(), reportingPeriods.toSet()),
        )
        checkThatAllIdentifiersWereAccepted(firstResponse, firstIdentifiers.size)
        val newRequestsAfter1stBulkRequest = getNewlyStoredRequestsAfterTimestamp(timeBeforeFirstBulkRequest)
        checkThatTheAmountOfNewlyStoredRequestsIsAsExpected(
            newRequestsAfter1stBulkRequest, firstIdentifiers.size * dataTypes.size * reportingPeriods.size,
        )
        checkThatBothRequestsExistExactlyOnceAfterBulkRequest(
            newRequestsAfter1stBulkRequest, dataTypes[0], reportingPeriods[0], companyId,
            identifierMapForUnknownCompany.keys.toList()[0], identifierMapForUnknownCompany.values.toList()[0],
        )
        val timestampBeforeSecondBulkRequest = retrieveTimeAndWaitOneMillisecond()
        val secondResponse = requestControllerApi.postBulkDataRequest(
            BulkDataRequest(secondIdentifiers, dataTypes.toSet(), reportingPeriods.toSet()),
        )
        checkThatAllIdentifiersWereAccepted(secondResponse, secondIdentifiers.size)
        val newRequestsAfter2ndBulkRequest = getNewlyStoredRequestsAfterTimestamp(timestampBeforeSecondBulkRequest)
        checkThatTheAmountOfNewlyStoredRequestsIsAsExpected(newRequestsAfter2ndBulkRequest, 0)
        val newRequestsAfter1stAnd2ndBulkRequest = getNewlyStoredRequestsAfterTimestamp(timeBeforeFirstBulkRequest)
        checkThatBothRequestsExistExactlyOnceAfterBulkRequest(
            newRequestsAfter1stAnd2ndBulkRequest, dataTypes[0], reportingPeriods[0], companyId,
            identifierMapForUnknownCompany.keys.toList()[0], identifierMapForUnknownCompany.values.toList()[0],
        )
    }

    @Test
    fun `post a bulk data request with at least one already existing request and check that this one is ignored`() {
        val leiForCompany = generateRandomLei()
        val isinForCompany = generateRandomIsin()
        val companyId = getIdForUploadedCompanyWithIdentifiers(leiForCompany, listOf(isinForCompany))
        val identifierMapForUnknownCompany = mapOf(DataRequestCompanyIdentifierType.lei to generateRandomLei())
        val frameworks = listOf(BulkDataRequest.DataTypes.lksg)
        val reportingPeriods = listOf("2023")
        val firstIdentifiers = setOf(leiForCompany, identifierMapForUnknownCompany.values.toList()[0])
        val secondIdentifiers = setOf(isinForCompany, identifierMapForUnknownCompany.values.toList()[0])
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
        val validIdentifiers = setOf(generateRandomLei(), generateRandomIsin(), generateRandomPermId())
        val dataTypes = enumValues<BulkDataRequest.DataTypes>().toSet()
        val reportingPeriods = setOf("2023")
        sendBulkRequestWithEmptyInputAndCheckErrorMessage(validIdentifiers, dataTypes, emptySet())
        sendBulkRequestWithEmptyInputAndCheckErrorMessage(validIdentifiers, emptySet(), reportingPeriods)
        sendBulkRequestWithEmptyInputAndCheckErrorMessage(emptySet(), dataTypes, reportingPeriods)
        sendBulkRequestWithEmptyInputAndCheckErrorMessage(validIdentifiers, emptySet(), emptySet())
        sendBulkRequestWithEmptyInputAndCheckErrorMessage(emptySet(), dataTypes, emptySet())
        sendBulkRequestWithEmptyInputAndCheckErrorMessage(emptySet(), emptySet(), reportingPeriods)
        sendBulkRequestWithEmptyInputAndCheckErrorMessage(emptySet(), emptySet(), emptySet())
        val invalidIdentifiers = setOf(
            generateRandomLei() + "F", generateRandomIsin() + "F", generateRandomPermId() + "F",
        )
        val clientException = causeClientExceptionByBulkDataRequest(invalidIdentifiers, dataTypes, reportingPeriods)
        checkErrorMessageForInvalidIdentifiersInBulkRequest(clientException)
    }

    private fun authenticateSendBulkRequestAndCheckAcceptedIdentifiers(
        technicalUser: TechnicalUser,
        identifiers: Set<String>,
        dataTypes: Set<BulkDataRequest.DataTypes>,
        reportingPeriods: Set<String>,
    ) {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(technicalUser)
        val responseForReader = requestControllerApi.postBulkDataRequest(
            BulkDataRequest(identifiers, dataTypes, reportingPeriods),
        )
        checkThatAllIdentifiersWereAccepted(responseForReader, identifiers.size)
    }

    @Test
    fun `post bulk data requests for different users and check that aggregation works properly`() {
        val leiForCompany = generateRandomLei()
        val companyId = getIdForUploadedCompanyWithIdentifiers(leiForCompany)
        val identifierMap = (
            generateMapWithOneRandomValueForEachIdentifierType() + mapOf(
                DataRequestCompanyIdentifierType.multipleRegexMatches to generateRandomPermId(20),
                DataRequestCompanyIdentifierType.datalandCompanyId to leiForCompany,
            )
            ).toMutableMap()
        val dataTypes = enumValues<BulkDataRequest.DataTypes>().toSet()
        val reportingPeriods = setOf("2022", "2023")
        TechnicalUser.entries.forEach {
            authenticateSendBulkRequestAndCheckAcceptedIdentifiers(
                it, identifierMap.values.toSet(), dataTypes, reportingPeriods,
            )
        }
        identifierMap[DataRequestCompanyIdentifierType.datalandCompanyId] = companyId
        val aggregatedDataRequests = requestControllerApi.getAggregatedDataRequests()
        iterateThroughFrameworksReportingPeriodsAndIdentifiersAndCheckAggregationWithCount(
            aggregatedDataRequests, dataTypes, reportingPeriods, identifierMap, TechnicalUser.entries.size.toLong(),
        )
    }

    private fun testNonTrivialIdentifierValueFilterOnAggregatedLevel(
        dataTypes: Set<BulkDataRequest.DataTypes>,
        reportingPeriods: Set<String>,
        identifiersToRecognizeMap: Map<DataRequestCompanyIdentifierType, String>,
        differentLei: String,
    ) {
        val aggregatedDataRequestsWithoutFilter = requestControllerApi.getAggregatedDataRequests(identifierValue = null)
        iterateThroughFrameworksReportingPeriodsAndIdentifiersAndCheckAggregationWithCount(
            aggregatedDataRequestsWithoutFilter, dataTypes, reportingPeriods,
            identifiersToRecognizeMap + mapOf(DataRequestCompanyIdentifierType.lei to differentLei), 1,
        )
        val aggregatedDataRequestsForEmptyString = requestControllerApi.getAggregatedDataRequests(identifierValue = "")
        iterateThroughFrameworksReportingPeriodsAndIdentifiersAndCheckAggregationWithCount(
            aggregatedDataRequestsForEmptyString, dataTypes, reportingPeriods,
            identifiersToRecognizeMap + mapOf(DataRequestCompanyIdentifierType.lei to differentLei), 1,
        )
    }

    @Test
    fun `post bulk data request and check that filter for the identifier value on aggregated level works properly`() {
        val permId = generateRandomPermId(10)
        val identifiersToRecognizeMap = mapOf(
            DataRequestCompanyIdentifierType.permId to permId,
            DataRequestCompanyIdentifierType.lei to permId + generateRandomLei().substring(10),
            DataRequestCompanyIdentifierType.isin to generateRandomIsin().substring(0, 2) + permId,
        )
        val differentLei = generateRandomLei()
        val identifiers = identifiersToRecognizeMap.values.toSet() + setOf(differentLei)
        val dataTypes = setOf(BulkDataRequest.DataTypes.lksg)
        val reportingPeriods = setOf("2023")
        val response = requestControllerApi.postBulkDataRequest(
            BulkDataRequest(identifiers, dataTypes, reportingPeriods),
        )
        checkThatAllIdentifiersWereAccepted(response, identifiers.size)
        val aggregatedDataRequests = requestControllerApi.getAggregatedDataRequests(identifierValue = permId)
        iterateThroughFrameworksReportingPeriodsAndIdentifiersAndCheckAggregationWithCount(
            aggregatedDataRequests, dataTypes, reportingPeriods, identifiersToRecognizeMap, 1,
        )
        assertFalse(aggregatedDataRequests.any { it.dataRequestCompanyIdentifierValue == differentLei })
        testNonTrivialIdentifierValueFilterOnAggregatedLevel(
            dataTypes, reportingPeriods, identifiersToRecognizeMap, differentLei,
        )
    }

    private fun checkAggregationForNonTrivialFrameworkFilter(
        frameworks: Set<BulkDataRequest.DataTypes>,
        reportingPeriods: Set<String>,
        identifierMap: Map<DataRequestCompanyIdentifierType, String>,
    ) {
        listOf(1, (2 until frameworks.size).random(), frameworks.size).forEach { numberOfRandomFrameworks ->
            val randomFrameworks = frameworks.shuffled().take(numberOfRandomFrameworks).toSet()
            val aggregatedDataRequests = requestControllerApi.getAggregatedDataRequests(
                dataTypes = randomFrameworks.map { findRequestControllerApiDataTypeForFramework(it) },
            )
            iterateThroughFrameworksReportingPeriodsAndIdentifiersAndCheckAggregationWithCount(
                aggregatedDataRequests, randomFrameworks, reportingPeriods, identifierMap, 1,
            )
            val frameworksNotToBeFound = frameworks.filter { !randomFrameworks.contains(it) }
            frameworksNotToBeFound.forEach { framework ->
                assertFalse(
                    aggregatedDataRequests.any {
                        it.dataType == findAggregatedDataRequestDataTypeForFramework(framework)
                    },
                )
            }
        }
    }

    @Test
    fun `post bulk requests and check that the filter for frameworks on aggregated level works properly`() {
        val dataTypes = enumValues<BulkDataRequest.DataTypes>().toSet()
        val reportingPeriods = setOf("2023")
        val identifierMap = mapOf(DataRequestCompanyIdentifierType.lei to generateRandomLei())
        val response = requestControllerApi.postBulkDataRequest(
            BulkDataRequest(identifierMap.values.toSet(), dataTypes, reportingPeriods),
        )
        checkThatAllIdentifiersWereAccepted(response, identifierMap.size)
        checkAggregationForNonTrivialFrameworkFilter(dataTypes, reportingPeriods, identifierMap)
        val aggregatedDataRequestsWithoutFilter = requestControllerApi.getAggregatedDataRequests(dataTypes = null)
        iterateThroughFrameworksReportingPeriodsAndIdentifiersAndCheckAggregationWithCount(
            aggregatedDataRequestsWithoutFilter, dataTypes, reportingPeriods, identifierMap, 1,
        )
        val aggregatedDataRequestsForEmptyList = requestControllerApi.getAggregatedDataRequests(dataTypes = emptyList())
        iterateThroughFrameworksReportingPeriodsAndIdentifiersAndCheckAggregationWithCount(
            aggregatedDataRequestsForEmptyList, dataTypes, reportingPeriods, identifierMap, 1,
        )
    }

    @Test
    fun `post bulk data request and check that the filter for reporting periods on aggregated level works properly`() {
        val identifierMap = mapOf(DataRequestCompanyIdentifierType.lei to generateRandomLei())
        val frameworks = setOf(BulkDataRequest.DataTypes.lksg)
        val reportingPeriods = setOf("2020", "2021", "2022", "2023")
        val response = requestControllerApi.postBulkDataRequest(
            BulkDataRequest(identifierMap.values.toSet(), frameworks, reportingPeriods),
        )
        checkThatAllIdentifiersWereAccepted(response, identifierMap.size)
        val randomReportingPeriod = reportingPeriods.random()
        val aggregatedDataRequests = requestControllerApi.getAggregatedDataRequests(
            reportingPeriod = randomReportingPeriod,
        )
        iterateThroughFrameworksReportingPeriodsAndIdentifiersAndCheckAggregationWithCount(
            aggregatedDataRequests, frameworks, setOf(randomReportingPeriod), identifierMap, 1,
        )
        reportingPeriods.filter { it != randomReportingPeriod }.forEach { filteredReportingPeriod ->
            assertFalse(aggregatedDataRequests.any { it.reportingPeriod == filteredReportingPeriod })
        }
        val aggregatedDataRequestsWithoutFilter = requestControllerApi.getAggregatedDataRequests(reportingPeriod = null)
        iterateThroughFrameworksReportingPeriodsAndIdentifiersAndCheckAggregationWithCount(
            aggregatedDataRequestsWithoutFilter, frameworks, reportingPeriods, identifierMap, 1,
        )
        val aggregatedDataRequestsForEmptyString = requestControllerApi.getAggregatedDataRequests(reportingPeriod = "")
        iterateThroughFrameworksReportingPeriodsAndIdentifiersAndCheckAggregationWithCount(
            aggregatedDataRequestsForEmptyString, frameworks, reportingPeriods, identifierMap, 1,
        )
    }
}
