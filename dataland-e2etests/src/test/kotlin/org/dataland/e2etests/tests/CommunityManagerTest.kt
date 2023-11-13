package org.dataland.e2etests.tests

import org.dataland.communitymanager.openApiClient.api.RequestControllerApi
import org.dataland.communitymanager.openApiClient.infrastructure.ClientException
import org.dataland.communitymanager.openApiClient.model.BulkDataRequest
import org.dataland.communitymanager.openApiClient.model.DataRequestCompanyIdentifierType
import org.dataland.communitymanager.openApiClient.model.StoredDataRequest
import org.dataland.e2etests.BASE_PATH_TO_COMMUNITY_MANAGER
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.checkErrorMessageForClientException
import org.dataland.e2etests.utils.checkThatAllIdentifiersWereAccepted
import org.dataland.e2etests.utils.checkThatMessageIsAsExpected
import org.dataland.e2etests.utils.checkThatRequestExistsExactlyOnceOnAggregateLevelWithCorrectCount
import org.dataland.e2etests.utils.checkThatRequestForFrameworkAndIdentifierExistsExactlyOnce
import org.dataland.e2etests.utils.checkThatTheAmountOfNewlyStoredRequestsIsAsExpected
import org.dataland.e2etests.utils.checkThatTheNumberOfAcceptedIdentifiersIsAsExpected
import org.dataland.e2etests.utils.checkThatTheNumberOfRejectedIdentifiersIsAsExpected
import org.dataland.e2etests.utils.findAggregatedDataRequestDataTypeForFramework
import org.dataland.e2etests.utils.findRequestControllerApiDataTypeForFramework
import org.dataland.e2etests.utils.generateMapWithOneRandomValueForEachIdentifierType
import org.dataland.e2etests.utils.generateRandomIsin
import org.dataland.e2etests.utils.generateRandomLei
import org.dataland.e2etests.utils.generateRandomPermId
import org.dataland.e2etests.utils.iterateThroughIdentifiersAndFrameworksAndCheckExistenceWithCount1
import org.dataland.e2etests.utils.retrieveTimeAndWaitOneMillisecond
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CommunityManagerTest {

    private val apiAccessor = ApiAccessor()
    private val requestControllerApi = RequestControllerApi(BASE_PATH_TO_COMMUNITY_MANAGER)

    private fun authenticateAsTechnicalUser(technicalUser: TechnicalUser) {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(technicalUser)
    }

    private fun getIdForUploadedCompanyWithIdentifiers(
        lei: String? = null,
        isins: List<String>? = null,
        permId: String? = null,
    ): String {
        return apiAccessor.uploadOneCompanyWithIdentifiers(lei, isins, permId)!!.actualStoredCompany.companyId
    }

    private fun getNewlyStoredRequestsAfterTimestamp(timestamp: Long): List<StoredDataRequest> {
        return requestControllerApi.getDataRequestsForUser().filter { storedDataRequest ->
            storedDataRequest.creationTimestamp > timestamp
        }
    }

    @BeforeAll
    fun authenticateAsReader() {
        authenticateAsTechnicalUser(TechnicalUser.Reader)
    }

    @Test
    fun `post bulk data request for all frameworks and different valid identifiers and check stored requests`() {
        val uniqueIdentifiersMap = generateMapWithOneRandomValueForEachIdentifierType()
        val multipleRegexMatchingIdentifier = generateRandomPermId(20)
        val identifiers = uniqueIdentifiersMap.values.toList() + listOf(multipleRegexMatchingIdentifier)
        val frameworks = enumValues<BulkDataRequest.ListOfFrameworkNames>().toList()
        val timestampBeforeBulkRequest = retrieveTimeAndWaitOneMillisecond()
        val response = requestControllerApi.postBulkDataRequest(
            BulkDataRequest(identifiers, frameworks),
        )
        checkThatAllIdentifiersWereAccepted(response, identifiers.size)
        val newlyStoredRequests = getNewlyStoredRequestsAfterTimestamp(timestampBeforeBulkRequest)
        checkThatTheAmountOfNewlyStoredRequestsIsAsExpected(newlyStoredRequests, identifiers.size * frameworks.size)
        val randomUniqueDataRequestCompanyIdentifierType = uniqueIdentifiersMap.keys.random()
        uniqueIdentifiersMap[randomUniqueDataRequestCompanyIdentifierType]?.let {
            checkThatRequestForFrameworkAndIdentifierExistsExactlyOnce(
                newlyStoredRequests, frameworks.random(), randomUniqueDataRequestCompanyIdentifierType, it,
            )
        }
        checkThatRequestForFrameworkAndIdentifierExistsExactlyOnce(
            newlyStoredRequests, frameworks.random(), DataRequestCompanyIdentifierType.multipleRegexMatches,
            multipleRegexMatchingIdentifier,
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
            ),
        )
        checkThatTheNumberOfAcceptedIdentifiersIsAsExpected(response, validIdentifiers.size)
        checkThatTheNumberOfRejectedIdentifiersIsAsExpected(response, invalidIdentifiers.size)
        checkThatMessageIsAsExpected(response, validIdentifiers.size, invalidIdentifiers.size)
        val newlyStoredRequests = getNewlyStoredRequestsAfterTimestamp(timestampBeforeBulkRequest)
        checkThatTheAmountOfNewlyStoredRequestsIsAsExpected(newlyStoredRequests, validIdentifiers.size)
        assertFalse(
            newlyStoredRequests.any { storedDataRequest ->
                invalidIdentifiers.contains(storedDataRequest.dataRequestCompanyIdentifierValue)
            },
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
        val timestampBeforeBulkRequest = retrieveTimeAndWaitOneMillisecond()
        val response = requestControllerApi.postBulkDataRequest(
            BulkDataRequest(identifiersForBulkRequest, frameworksForBulkRequest),
        )
        checkThatAllIdentifiersWereAccepted(response, identifiersForBulkRequest.size)
        val newlyStoredRequests = getNewlyStoredRequestsAfterTimestamp(timestampBeforeBulkRequest)
        checkThatTheAmountOfNewlyStoredRequestsIsAsExpected(
            newlyStoredRequests,
            (identifiersForBulkRequest.size - 1) * frameworksForBulkRequest.size,
        )
        checkThatRequestForFrameworkAndIdentifierExistsExactlyOnce(
            newlyStoredRequests, frameworksForBulkRequest[0],
            DataRequestCompanyIdentifierType.datalandCompanyId, companyId,
        )
        checkThatRequestForFrameworkAndIdentifierExistsExactlyOnce(
            newlyStoredRequests, frameworksForBulkRequest[0],
            identifierTypeForUnknownCompany, identifierValueForUnknownCompany,
        )
    }

    private fun checkThatBothRequestExistExactlyOnceAfterBulkRequest(
        requestsStoredAfterBulkRequest: List<StoredDataRequest>,
        framework: BulkDataRequest.ListOfFrameworkNames,
        companyId: String,
        identifierTypeForUnknownCompany: DataRequestCompanyIdentifierType,
        identifierValueForUnknownCompany: String,
    ) {
        checkThatRequestForFrameworkAndIdentifierExistsExactlyOnce(
            requestsStoredAfterBulkRequest,
            framework,
            DataRequestCompanyIdentifierType.datalandCompanyId,
            companyId,
        )
        checkThatRequestForFrameworkAndIdentifierExistsExactlyOnce(
            requestsStoredAfterBulkRequest,
            framework,
            identifierTypeForUnknownCompany,
            identifierValueForUnknownCompany,
        )
    }

    @Test
    fun `post a bulk data request with at least one already existing request and check that this one is ignored`() {
        val leiForCompany = generateRandomLei()
        val isinForCompany = generateRandomIsin()
        val companyId = getIdForUploadedCompanyWithIdentifiers(leiForCompany, listOf(isinForCompany))
        val identifierMapForUnknownCompany = mapOf(DataRequestCompanyIdentifierType.lei to generateRandomLei())
        val frameworks = listOf(BulkDataRequest.ListOfFrameworkNames.lksg)
        val firstIdentifiers = listOf(leiForCompany, identifierMapForUnknownCompany.values.toList()[0])
        val timeBeforeFirstBulkRequest = retrieveTimeAndWaitOneMillisecond()
        val firstResponse = requestControllerApi.postBulkDataRequest(BulkDataRequest(firstIdentifiers, frameworks))
        checkThatAllIdentifiersWereAccepted(firstResponse, firstIdentifiers.size)
        val newRequestsAfter1stBulkRequest = getNewlyStoredRequestsAfterTimestamp(timeBeforeFirstBulkRequest)
        checkThatTheAmountOfNewlyStoredRequestsIsAsExpected(
            newRequestsAfter1stBulkRequest, firstIdentifiers.size * frameworks.size,
        )
        checkThatBothRequestExistExactlyOnceAfterBulkRequest(
            newRequestsAfter1stBulkRequest, frameworks[0], companyId,
            identifierMapForUnknownCompany.keys.toList()[0], identifierMapForUnknownCompany.values.toList()[0],
        )
        val secondIdentifiers = listOf(isinForCompany, identifierMapForUnknownCompany.values.toList()[0])
        val timestampBeforeSecondBulkRequest = retrieveTimeAndWaitOneMillisecond()
        val secondResponse = requestControllerApi.postBulkDataRequest(BulkDataRequest(secondIdentifiers, frameworks))
        checkThatAllIdentifiersWereAccepted(secondResponse, secondIdentifiers.size)
        val newRequestsAfter2ndBulkRequest = getNewlyStoredRequestsAfterTimestamp(timestampBeforeSecondBulkRequest)
        checkThatTheAmountOfNewlyStoredRequestsIsAsExpected(newRequestsAfter2ndBulkRequest, 0)
        val newRequestsAfter1stAnd2ndBulkRequest = getNewlyStoredRequestsAfterTimestamp(timeBeforeFirstBulkRequest)
        checkThatBothRequestExistExactlyOnceAfterBulkRequest(
            newRequestsAfter1stAnd2ndBulkRequest, frameworks[0], companyId,
            identifierMapForUnknownCompany.keys.toList()[0], identifierMapForUnknownCompany.values.toList()[0],
        )
    }

    @Test
    fun `check the expected exception is thrown when frameworks are empty or identifiers are empty or invalid only`() {
        val validIdentifiers = listOf(generateRandomLei(), generateRandomIsin(), generateRandomPermId())
        val frameworks = enumValues<BulkDataRequest.ListOfFrameworkNames>().toList()
        val exceptionForEmptyFrameworkList = assertThrows<ClientException> {
            requestControllerApi.postBulkDataRequest(BulkDataRequest(validIdentifiers, emptyList()))
        }
        checkErrorMessageForClientException(exceptionForEmptyFrameworkList)
        val exceptionForEmptyIdentifiersList = assertThrows<ClientException> {
            requestControllerApi.postBulkDataRequest(BulkDataRequest(emptyList(), frameworks))
        }
        checkErrorMessageForClientException(exceptionForEmptyIdentifiersList)
        val exceptionForEmptyFrameworksAndEmptyIdentifiersList = assertThrows<ClientException> {
            requestControllerApi.postBulkDataRequest(BulkDataRequest(emptyList(), emptyList()))
        }
        checkErrorMessageForClientException(exceptionForEmptyFrameworksAndEmptyIdentifiersList)
        val invalidIdentifiers = listOf(
            generateRandomLei() + "F", generateRandomIsin() + "F", generateRandomPermId() + "F",
        )
        val exceptionForInvalidIdentifiersOnly = assertThrows<ClientException> {
            requestControllerApi.postBulkDataRequest(BulkDataRequest(invalidIdentifiers, frameworks))
        }
        checkErrorMessageForClientException(exceptionForInvalidIdentifiersOnly)
    }

    private fun authenticateSendBulkRequestAndCheckAcceptedIdentifiers(
        technicalUser: TechnicalUser,
        identifiers: List<String>,
        frameworks: List<BulkDataRequest.ListOfFrameworkNames>,
    ) {
        authenticateAsTechnicalUser(technicalUser)
        val responseForReader = requestControllerApi.postBulkDataRequest(
            BulkDataRequest(identifiers, frameworks),
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
        val frameworks = enumValues<BulkDataRequest.ListOfFrameworkNames>().toList()
        TechnicalUser.values().forEach { technicalUser ->
            authenticateSendBulkRequestAndCheckAcceptedIdentifiers(
                technicalUser, identifierMap.values.toList(), frameworks,
            )
        }
        identifierMap[DataRequestCompanyIdentifierType.datalandCompanyId] = companyId
        val aggregatedDataRequests = requestControllerApi.getAggregatedDataRequests()
        frameworks.forEach { framework ->
            identifierMap.forEach { (identifierType, identifierValue) ->
                checkThatRequestExistsExactlyOnceOnAggregateLevelWithCorrectCount(
                    aggregatedDataRequests,
                    framework,
                    identifierType,
                    identifierValue,
                    TechnicalUser.values().size.toLong(),
                )
            }
        }
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
        val identifiers = identifiersToRecognizeMap.values.toList() + listOf(differentLei)
        val frameworks = listOf(BulkDataRequest.ListOfFrameworkNames.lksg)
        val response = requestControllerApi.postBulkDataRequest(BulkDataRequest(identifiers, frameworks))
        checkThatAllIdentifiersWereAccepted(response, identifiers.size)
        val aggregatedDataRequests = requestControllerApi.getAggregatedDataRequests(identifierValue = permId)
        iterateThroughIdentifiersAndFrameworksAndCheckExistenceWithCount1(
            identifiersToRecognizeMap, frameworks, aggregatedDataRequests,
        )
        assertFalse(aggregatedDataRequests.any { it.dataRequestCompanyIdentifierValue == differentLei })
        val aggregatedDataRequestsForEmptyString = requestControllerApi.getAggregatedDataRequests(identifierValue = "")
        iterateThroughIdentifiersAndFrameworksAndCheckExistenceWithCount1(
            identifiersToRecognizeMap, frameworks, aggregatedDataRequestsForEmptyString,
        )
        iterateThroughIdentifiersAndFrameworksAndCheckExistenceWithCount1(
            mapOf(DataRequestCompanyIdentifierType.lei to differentLei),
            frameworks,
            aggregatedDataRequestsForEmptyString,
        )
    }

    @Test
    fun `post bulk requests and check that the filter for frameworks on aggregated level works properly`() {
        val frameworks = enumValues<BulkDataRequest.ListOfFrameworkNames>().toList()
        val identifierMap = mapOf(DataRequestCompanyIdentifierType.lei to generateRandomLei())
        val response = requestControllerApi.postBulkDataRequest(
            BulkDataRequest(identifierMap.values.toList(), frameworks),
        )
        checkThatAllIdentifiersWereAccepted(response, identifierMap.size)
        listOf(1, (2 until frameworks.size).random(), frameworks.size).forEach { numberOfRandomFrameworks ->
            val randomFrameworks = frameworks.shuffled().take(numberOfRandomFrameworks)
            val aggregatedDataRequests = requestControllerApi.getAggregatedDataRequests(
                dataTypes = randomFrameworks.map { findRequestControllerApiDataTypeForFramework(it) },
            )
            iterateThroughIdentifiersAndFrameworksAndCheckExistenceWithCount1(
                identifierMap, randomFrameworks, aggregatedDataRequests,
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
        val aggregatedDataRequestsForEmptyList = requestControllerApi.getAggregatedDataRequests(dataTypes = emptyList())
        iterateThroughIdentifiersAndFrameworksAndCheckExistenceWithCount1(
            identifierMap, frameworks, aggregatedDataRequestsForEmptyList,
        )
    }
}
