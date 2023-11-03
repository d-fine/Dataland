package org.dataland.e2etests.tests

import org.dataland.communitymanager.openApiClient.api.RequestControllerApi
import org.dataland.communitymanager.openApiClient.infrastructure.ClientException
import org.dataland.communitymanager.openApiClient.model.BulkDataRequest
import org.dataland.communitymanager.openApiClient.model.BulkDataRequestResponse
import org.dataland.communitymanager.openApiClient.model.DataRequestCompanyIdentifierType
import org.dataland.communitymanager.openApiClient.model.DataRequestEntity
import org.dataland.e2etests.BASE_PATH_TO_COMMUNITY_MANAGER
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import java.time.Instant

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CommunityManagerTest {

    private val apiAccessor = ApiAccessor()
    private val requestControllerApi = RequestControllerApi(BASE_PATH_TO_COMMUNITY_MANAGER)

    private fun generateRandomLei(): String {
        val digits = ('0'..'9')
        val combinations = ('A'..'Z') + digits
        val patternSection1 = (1..18).map { combinations.random() }.joinToString("")
        val patternSection2 = (1..2).map { digits.random() }.joinToString("")
        return patternSection1 + patternSection2
    }

    private fun generateRandomIsin(): String {
        val upperCaseLetters = ('A'..'Z')
        val combinations = upperCaseLetters + ('0'..'9')
        val patternSection1 = (1..2).map { upperCaseLetters.random() }.joinToString("")
        val patternSection2 = (1..10).map { combinations.random() }.joinToString("")
        return patternSection1 + patternSection2
    }

    private fun generateRandomPermId(multipleRegexMatchingDesired: Boolean = false): String {
        fun generateRandomIntegerDifferentFrom20(): Int {
            val randomInt = (1..100).random()
            return if (randomInt != 20) randomInt else generateRandomIntegerDifferentFrom20()
        }

        val digits = ('0'..'9')
        val numberOfCharacters = if (multipleRegexMatchingDesired) 20 else generateRandomIntegerDifferentFrom20()
        return (1..numberOfCharacters).map { digits.random() }.joinToString("")
    }

    private fun checkThatTheNumberOfAcceptedIdentifiersIsAsExpected(
        requestResponse: BulkDataRequestResponse,
        expectedNumberOfAcceptedIdentifiers: Int,
    ) {
        assertEquals(
            expectedNumberOfAcceptedIdentifiers,
            requestResponse.acceptedCompanyIdentifiers.size,
            "Not every combination of identifier and framework was sent as a request as expected.",
        )
    }

    private fun checkThatTheNumberOfRejectedIdentifiersIsAsExpected(
        requestResponse: BulkDataRequestResponse,
        expectedNumberOfRejectedIdentifiers: Int,
    ) {
        assertEquals(
            expectedNumberOfRejectedIdentifiers,
            requestResponse.rejectedCompanyIdentifiers.size,
            "Not all identifiers were accepted as expected.",
        )
    }

    private fun checkThatMessageIsAsExpected(
        requestResponse: BulkDataRequestResponse,
        expectedNumberOfAcceptedIdentifiers: Int,
        expectedNumberOfRejectedIdentifiers: Int,
    ) {
        val errorMessage = "The message sent as part of the response to the bulk data request is not as expected."
        if (expectedNumberOfRejectedIdentifiers == 0) {
            assertEquals(
                "$expectedNumberOfAcceptedIdentifiers distinct company identifiers were accepted.",
                requestResponse.message,
                errorMessage,
            )
        } else {
            assertEquals(
                "$expectedNumberOfRejectedIdentifiers of your " +
                    "${expectedNumberOfAcceptedIdentifiers + expectedNumberOfRejectedIdentifiers} distinct company " +
                    "identifiers were rejected because of a format that is not matching a valid LEI, ISIN or PermId.",
                requestResponse.message,
                errorMessage,
            )
        }
    }

    private fun checkThatAllIdentifiersWereAccepted(
        requestResponse: BulkDataRequestResponse,
        expectedNumberOfAcceptedIdentifiers: Int,
    ) {
        checkThatTheNumberOfAcceptedIdentifiersIsAsExpected(requestResponse, expectedNumberOfAcceptedIdentifiers)
        checkThatTheNumberOfRejectedIdentifiersIsAsExpected(requestResponse, 0)
        checkThatMessageIsAsExpected(requestResponse, expectedNumberOfAcceptedIdentifiers, 0)
    }

    private fun checkThatTheAmountOfNewlyStoredRequestsIsAsExpected(
        recentlyStoredRequestsForUser: List<DataRequestEntity>,
        expectedNumberOfNewlyStoredRequests: Int,
    ) {
        assertEquals(
            expectedNumberOfNewlyStoredRequests,
            recentlyStoredRequestsForUser.size,
            "The number of individual requests stored does not match the expected amount.",
        )
    }

    private fun checkThatRequestForDataTypeAndIdentifierExistsExactlyOnce(
        recentlyStoredRequestsForUser: List<DataRequestEntity>,
        dataType: DataRequestEntity.DataType,
        dataRequestCompanyIdentifierType: DataRequestCompanyIdentifierType,
        dataRequestCompanyIdentifierValue: String,
    ) {
        assertEquals(
            1,
            recentlyStoredRequestsForUser.filter { dataRequestEntity ->
                dataRequestEntity.dataType == dataType &&
                    dataRequestEntity.dataRequestCompanyIdentifierType == dataRequestCompanyIdentifierType &&
                    dataRequestEntity.dataRequestCompanyIdentifierValue == dataRequestCompanyIdentifierValue
            }.size,
            "For the ${dataRequestCompanyIdentifierType.value} $dataRequestCompanyIdentifierValue " +
                "and the framework ${dataType.value} there is not exactly one newly stored request as expected.",
        )
    }

    private fun retrieveTimeAndWaitOneMillisecond(): Long {
        val timestamp = Instant.now().toEpochMilli()
        Thread.sleep(1)
        return timestamp
    }

    private fun getNewlyStoredRequestsAfterTimestamp(timestamp: Long): List<DataRequestEntity> {
        return requestControllerApi.getDataRequestsForUser().filter { dataRequestEntity ->
            dataRequestEntity.creationTimestamp!! > timestamp
        }
    }

    private fun findDataTypeForFramework(framework: BulkDataRequest.ListOfFrameworkNames): DataRequestEntity.DataType {
        return DataRequestEntity.DataType.values().find { dataType -> dataType.value == framework.value }!!
    }

    private fun authenticateAsTechnicalUser(technicalUser: TechnicalUser) {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(technicalUser)
    }

    @Test
    fun `post bulk data request for all frameworks and different valid identifiers and check stored requests`() {
        authenticateAsTechnicalUser(TechnicalUser.Reader)
        val uniqueIdentifiersMap = mapOf(
            DataRequestCompanyIdentifierType.lei to generateRandomLei(),
            DataRequestCompanyIdentifierType.isin to generateRandomIsin(),
            DataRequestCompanyIdentifierType.permId to generateRandomPermId(),
        )
        val multipleRegexMatchingIdentifier = generateRandomPermId(true)
        val identifiers = uniqueIdentifiersMap.values.toList() + listOf(multipleRegexMatchingIdentifier)
        val frameworks = enumValues<BulkDataRequest.ListOfFrameworkNames>().toList().filter { listOfFrameworkNames ->
            listOfFrameworkNames != BulkDataRequest.ListOfFrameworkNames.eutaxonomyMinusFinancials &&
                listOfFrameworkNames != BulkDataRequest.ListOfFrameworkNames.eutaxonomyMinusNonMinusFinancials
        } // TODO Filter to be removed once EU Taxo works with standard value including "-"
        val timestampBeforeBulkRequest = retrieveTimeAndWaitOneMillisecond()
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        val response = requestControllerApi.postBulkDataRequest(
            BulkDataRequest(identifiers, frameworks),
        )
        checkThatAllIdentifiersWereAccepted(response, identifiers.size)
        val newlyStoredRequests = getNewlyStoredRequestsAfterTimestamp(timestampBeforeBulkRequest)
        checkThatTheAmountOfNewlyStoredRequestsIsAsExpected(newlyStoredRequests, identifiers.size * frameworks.size)
        val randomDataType = findDataTypeForFramework(frameworks.random())
        // TODO Try to remove different types of DataTypeEnum (referenced in BulkDataRequest and in DataRequestEntity)
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

    private fun getIdForUploadedCompanyWithIdentifiers(
        lei: String? = null,
        isin: List<String>? = null,
        permId: String? = null,
    ): String {
        return apiAccessor.uploadOneCompanyWithIdentifiers(lei, isin, permId)!!.actualStoredCompany.companyId
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
        val dataType = findDataTypeForFramework(frameworksForBulkRequest[0])
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
        val dataType = findDataTypeForFramework(frameworksForBulkRequest[0])
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

    private fun checkErrorMessageForClientException(clientException: ClientException) {
        assertEquals("Client error : 400 ", clientException.message)
    }

    @Test
    fun `check the expected exception thrown when frameworks are empty or identifiers are empty or invalid only`() {
        authenticateAsTechnicalUser(TechnicalUser.Reader)
        val validIdentifiers = listOf(generateRandomLei(), generateRandomIsin(), generateRandomPermId())
        val frameworks = enumValues<BulkDataRequest.ListOfFrameworkNames>().toList().filter { listOfFrameworkNames ->
            listOfFrameworkNames != BulkDataRequest.ListOfFrameworkNames.eutaxonomyMinusFinancials &&
                listOfFrameworkNames != BulkDataRequest.ListOfFrameworkNames.eutaxonomyMinusNonMinusFinancials
        } // TODO Filter to be removed once EU Taxo works with standard value including "-"
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
        val identifierMap = mapOf(
            DataRequestCompanyIdentifierType.lei to generateRandomLei(),
            DataRequestCompanyIdentifierType.isin to generateRandomIsin(),
            DataRequestCompanyIdentifierType.permId to generateRandomPermId(),
        )
        val frameworks = enumValues<BulkDataRequest.ListOfFrameworkNames>().toList().filter { listOfFrameworkNames ->
            listOfFrameworkNames != BulkDataRequest.ListOfFrameworkNames.eutaxonomyMinusFinancials &&
                listOfFrameworkNames != BulkDataRequest.ListOfFrameworkNames.eutaxonomyMinusNonMinusFinancials
        } // TODO Filter to be removed once EU Taxo works with standard value including "-"
        TechnicalUser.values().forEach { technicalUser ->
            authenticateSendBulkRequestAndCheckAcceptedIdentifiers(
                technicalUser, identifierMap.values.toList(), frameworks,
            )
        }
    }
}
