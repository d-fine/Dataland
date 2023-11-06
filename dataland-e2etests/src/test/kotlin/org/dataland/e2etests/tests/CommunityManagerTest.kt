package org.dataland.e2etests.tests

import org.dataland.communitymanager.openApiClient.api.RequestControllerApi
import org.dataland.communitymanager.openApiClient.infrastructure.ClientException
import org.dataland.communitymanager.openApiClient.model.BulkDataRequest
import org.dataland.communitymanager.openApiClient.model.DataRequestCompanyIdentifierType
import org.dataland.communitymanager.openApiClient.model.DataRequestEntity
import org.dataland.e2etests.BASE_PATH_TO_COMMUNITY_MANAGER
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.checkErrorMessageForClientException
import org.dataland.e2etests.utils.checkThatAllIdentifiersWereAccepted
import org.dataland.e2etests.utils.checkThatMessageIsAsExpected
import org.dataland.e2etests.utils.checkThatRequestExistsExactlyOnceOnAggregateLevelWithCorrectCount
import org.dataland.e2etests.utils.checkThatRequestForDataTypeAndIdentifierExistsExactlyOnce
import org.dataland.e2etests.utils.checkThatTheAmountOfNewlyStoredRequestsIsAsExpected
import org.dataland.e2etests.utils.checkThatTheNumberOfAcceptedIdentifiersIsAsExpected
import org.dataland.e2etests.utils.checkThatTheNumberOfRejectedIdentifiersIsAsExpected
import org.dataland.e2etests.utils.findAggregatedDataRequestDataTypeForFramework
import org.dataland.e2etests.utils.findDataRequestEntityDataTypeForFramework
import org.dataland.e2etests.utils.findRequestControllerApiDataTypeForFramework
import org.dataland.e2etests.utils.generateRandomIsin
import org.dataland.e2etests.utils.generateRandomLei
import org.dataland.e2etests.utils.generateRandomPermId
import org.dataland.e2etests.utils.iterateThroughIdentifiersAndFrameworksAndCheckExistenceWithCount1
import org.dataland.e2etests.utils.retrieveTimeAndWaitOneMillisecond
import org.junit.jupiter.api.Assertions.assertFalse
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
        isin: List<String>? = null,
        permId: String? = null,
    ): String {
        return apiAccessor.uploadOneCompanyWithIdentifiers(lei, isin, permId)!!.actualStoredCompany.companyId
    }

    private fun getNewlyStoredRequestsAfterTimestamp(timestamp: Long): List<DataRequestEntity> {
        return requestControllerApi.getDataRequestsForUser().filter { dataRequestEntity ->
            dataRequestEntity.creationTimestamp > timestamp
        }
    }

    @Test
    fun `post bulk data request for all frameworks and different valid identifiers and check stored requests`() {
        authenticateAsTechnicalUser(TechnicalUser.Reader)
        val uniqueIdentifiersMap = mapOf(
            DataRequestCompanyIdentifierType.lei to generateRandomLei(),
            DataRequestCompanyIdentifierType.isin to generateRandomIsin(),
            DataRequestCompanyIdentifierType.permId to generateRandomPermId(),
        )
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
        val randomDataType = findDataRequestEntityDataTypeForFramework(frameworks.random())
        val randomUniqueDataRequestCompanyIdentifierType = uniqueIdentifiersMap.keys.random()
        checkThatRequestForDataTypeAndIdentifierExistsExactlyOnce(
            newlyStoredRequests,
            randomDataType,
            randomUniqueDataRequestCompanyIdentifierType,
            uniqueIdentifiersMap[randomUniqueDataRequestCompanyIdentifierType]!!,
        )
        checkThatRequestForDataTypeAndIdentifierExistsExactlyOnce(
            newlyStoredRequests,
            randomDataType,
            DataRequestCompanyIdentifierType.multipleRegexMatches,
            multipleRegexMatchingIdentifier,
        )
    }

    @Test
    fun `post a bulk data request with at least one invalid identifier and check that this gives no stored request`() {
        authenticateAsTechnicalUser(TechnicalUser.Reader)
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
            newlyStoredRequests.any { dataRequestEntity ->
                invalidIdentifiers.contains(dataRequestEntity.dataRequestCompanyIdentifierValue)
            },
        )
    }

    @Test
    fun `post bulk data request with at least one company duplicate and check that only one request is stored`() {
        authenticateAsTechnicalUser(TechnicalUser.Reader)
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
        val dataType = findDataRequestEntityDataTypeForFramework(frameworksForBulkRequest[0])
        checkThatRequestForDataTypeAndIdentifierExistsExactlyOnce(
            newlyStoredRequests,
            dataType,
            DataRequestCompanyIdentifierType.datalandCompanyId,
            companyId,
        )
        checkThatRequestForDataTypeAndIdentifierExistsExactlyOnce(
            newlyStoredRequests,
            dataType,
            identifierTypeForUnknownCompany,
            identifierValueForUnknownCompany,
        )
    }

    private fun checkThatBothRequestExistExactlyOnceAfterBulkRequest(
        requestsStoredAfterBulkRequest: List<DataRequestEntity>,
        dataType: DataRequestEntity.DataType,
        companyId: String,
        identifierTypeForUnknownCompany: DataRequestCompanyIdentifierType,
        identifierValueForUnknownCompany: String,
    ) {
        checkThatRequestForDataTypeAndIdentifierExistsExactlyOnce(
            requestsStoredAfterBulkRequest,
            dataType,
            DataRequestCompanyIdentifierType.datalandCompanyId,
            companyId,
        )
        checkThatRequestForDataTypeAndIdentifierExistsExactlyOnce(
            requestsStoredAfterBulkRequest,
            dataType,
            identifierTypeForUnknownCompany,
            identifierValueForUnknownCompany,
        )
    }

    @Test
    fun `post a bulk data request with at least one already existing request and check that this one is ignored`() {
        authenticateAsTechnicalUser(TechnicalUser.Reader)
        val leiForCompany = generateRandomLei()
        val isinForCompany = generateRandomIsin()
        val companyId = getIdForUploadedCompanyWithIdentifiers(leiForCompany, listOf(isinForCompany))
        val identifierTypeForUnknownCompany = DataRequestCompanyIdentifierType.lei
        val identifierValueForUnknownCompany = generateRandomLei()
        val frameworksForBulkRequest = listOf(BulkDataRequest.ListOfFrameworkNames.lksg)
        val identifiersForFirstBulkRequest = listOf(leiForCompany, identifierValueForUnknownCompany)
        val timeBeforeFirstBulkRequest = retrieveTimeAndWaitOneMillisecond()
        val firstResponse = requestControllerApi.postBulkDataRequest(
            BulkDataRequest(identifiersForFirstBulkRequest, frameworksForBulkRequest),
        )
        checkThatAllIdentifiersWereAccepted(firstResponse, identifiersForFirstBulkRequest.size)
        val newlyStoredRequestsAfterFirstBulkRequest = getNewlyStoredRequestsAfterTimestamp(timeBeforeFirstBulkRequest)
        val dataType = findDataRequestEntityDataTypeForFramework(frameworksForBulkRequest[0])
        checkThatTheAmountOfNewlyStoredRequestsIsAsExpected(
            newlyStoredRequestsAfterFirstBulkRequest,
            identifiersForFirstBulkRequest.size * frameworksForBulkRequest.size,
        )
        checkThatBothRequestExistExactlyOnceAfterBulkRequest(
            newlyStoredRequestsAfterFirstBulkRequest,
            dataType,
            companyId,
            identifierTypeForUnknownCompany,
            identifierValueForUnknownCompany,
        )
        val identifiersForSecondBulkRequest = listOf(isinForCompany, identifierValueForUnknownCompany)
        val timestampBeforeSecondBulkRequest = retrieveTimeAndWaitOneMillisecond()
        val secondResponse = requestControllerApi.postBulkDataRequest(
            BulkDataRequest(identifiersForSecondBulkRequest, frameworksForBulkRequest),
        )
        checkThatAllIdentifiersWereAccepted(secondResponse, identifiersForSecondBulkRequest.size)
        val newlyStoredRequestsAfterSecondBulkRequest = getNewlyStoredRequestsAfterTimestamp(
            timestampBeforeSecondBulkRequest,
        )
        checkThatTheAmountOfNewlyStoredRequestsIsAsExpected(newlyStoredRequestsAfterSecondBulkRequest, 0)
        val newlyStoredRequestsAfterFirstAndSecondBulkRequest = getNewlyStoredRequestsAfterTimestamp(
            timeBeforeFirstBulkRequest,
        )
        checkThatBothRequestExistExactlyOnceAfterBulkRequest(
            newlyStoredRequestsAfterFirstAndSecondBulkRequest,
            dataType,
            companyId,
            identifierTypeForUnknownCompany,
            identifierValueForUnknownCompany,
        )
    }

    @Test
    fun `check the expected exception is thrown when frameworks are empty or identifiers are empty or invalid only`() {
        authenticateAsTechnicalUser(TechnicalUser.Reader)
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
        val identifierMap = mutableMapOf(
            DataRequestCompanyIdentifierType.lei to generateRandomLei(),
            DataRequestCompanyIdentifierType.isin to generateRandomIsin(),
            DataRequestCompanyIdentifierType.permId to generateRandomPermId(),
            DataRequestCompanyIdentifierType.multipleRegexMatches to generateRandomPermId(20),
            DataRequestCompanyIdentifierType.datalandCompanyId to leiForCompany,
        )
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
        authenticateAsTechnicalUser(TechnicalUser.Reader)
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
        val aggregatedDataRequestsForEmptyStringFilter = requestControllerApi.getAggregatedDataRequests(
            identifierValue = "",
        )
        iterateThroughIdentifiersAndFrameworksAndCheckExistenceWithCount1(
            identifiersToRecognizeMap, frameworks, aggregatedDataRequestsForEmptyStringFilter,
        )
        iterateThroughIdentifiersAndFrameworksAndCheckExistenceWithCount1(
            mapOf(DataRequestCompanyIdentifierType.lei to differentLei),
            frameworks,
            aggregatedDataRequestsForEmptyStringFilter,
        )
    }

    @Test
    fun `post bulk requests and check that the filter for frameworks on aggregated level works properly`() {
        authenticateAsTechnicalUser(TechnicalUser.Reader)
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
        val aggregatedDataRequestsForEmptyListFilter = requestControllerApi.getAggregatedDataRequests(
            dataTypes = emptyList(),
        )
        iterateThroughIdentifiersAndFrameworksAndCheckExistenceWithCount1(
            identifierMap, frameworks, aggregatedDataRequestsForEmptyListFilter,
        )
    }
}
