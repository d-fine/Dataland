package org.dataland.e2etests.utils

import org.dataland.communitymanager.openApiClient.api.RequestControllerApi
import org.dataland.communitymanager.openApiClient.infrastructure.ClientException
import org.dataland.communitymanager.openApiClient.model.AggregatedDataRequest
import org.dataland.communitymanager.openApiClient.model.BulkDataRequest
import org.dataland.communitymanager.openApiClient.model.BulkDataRequestResponse
import org.dataland.communitymanager.openApiClient.model.DataRequestCompanyIdentifierType
import org.dataland.communitymanager.openApiClient.model.DataRequestEntity
import org.junit.jupiter.api.Assertions.assertEquals
import java.time.Instant

fun retrieveTimeAndWaitOneMillisecond(): Long {
    val timestamp = Instant.now().toEpochMilli()
    Thread.sleep(1)
    return timestamp
}

// TODO Try to remove the following three different DataType variations coming from using the backend enum in ...
// TODO ... three different cases
fun findDataRequestEntityDataTypeForFramework(
    framework: BulkDataRequest.ListOfFrameworkNames,
): DataRequestEntity.DataType {
    return DataRequestEntity.DataType.values().find { dataType -> dataType.value == framework.value }!!
}
fun findAggregatedDataRequestDataTypeForFramework(
    framework: BulkDataRequest.ListOfFrameworkNames,
): AggregatedDataRequest.DataType {
    return AggregatedDataRequest.DataType.values().find { dataType -> dataType.value == framework.value }!!
}
fun findRequestControllerApiDataTypeForFramework(
    framework: BulkDataRequest.ListOfFrameworkNames,
): RequestControllerApi.DataTypesGetAggregatedDataRequests {
    return RequestControllerApi.DataTypesGetAggregatedDataRequests.values().find { dataType ->
        dataType.value == framework.value
    }!!
}

fun generateRandomLei(): String {
    val digits = ('0'..'9')
    val combinations = ('A'..'Z') + digits
    val patternSection1 = (1..18).map { combinations.random() }.joinToString("")
    val patternSection2 = (1..2).map { digits.random() }.joinToString("")
    return patternSection1 + patternSection2
}

fun generateRandomIsin(): String {
    val upperCaseLetters = ('A'..'Z')
    val combinations = upperCaseLetters + ('0'..'9')
    val patternSection1 = (1..2).map { upperCaseLetters.random() }.joinToString("")
    val patternSection2 = (1..10).map { combinations.random() }.joinToString("")
    return patternSection1 + patternSection2
}

fun generateRandomPermId(numberOfDigits: Int? = null): String {
    fun generateRandomIntegerDifferentFrom20(): Int {
        val randomInt = (1..100).random()
        return if (randomInt != 20) randomInt else generateRandomIntegerDifferentFrom20()
    }

    val digits = ('0'..'9')
    val numberOfCharacters = numberOfDigits ?: generateRandomIntegerDifferentFrom20()
    return (1..numberOfCharacters).map { digits.random() }.joinToString("")
}

fun checkThatTheNumberOfAcceptedIdentifiersIsAsExpected(
    requestResponse: BulkDataRequestResponse,
    expectedNumberOfAcceptedIdentifiers: Int,
) {
    assertEquals(
        expectedNumberOfAcceptedIdentifiers,
        requestResponse.acceptedCompanyIdentifiers.size,
        "Not every combination of identifier and framework was sent as a request as expected.",
    )
}

fun checkThatTheNumberOfRejectedIdentifiersIsAsExpected(
    requestResponse: BulkDataRequestResponse,
    expectedNumberOfRejectedIdentifiers: Int,
) {
    assertEquals(
        expectedNumberOfRejectedIdentifiers,
        requestResponse.rejectedCompanyIdentifiers.size,
        "Not all identifiers were accepted as expected.",
    )
}

fun checkThatMessageIsAsExpected(
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

fun checkThatAllIdentifiersWereAccepted(
    requestResponse: BulkDataRequestResponse,
    expectedNumberOfAcceptedIdentifiers: Int,
) {
    checkThatTheNumberOfAcceptedIdentifiersIsAsExpected(requestResponse, expectedNumberOfAcceptedIdentifiers)
    checkThatTheNumberOfRejectedIdentifiersIsAsExpected(requestResponse, 0)
    checkThatMessageIsAsExpected(requestResponse, expectedNumberOfAcceptedIdentifiers, 0)
}

fun checkThatTheAmountOfNewlyStoredRequestsIsAsExpected(
    recentlyStoredRequestsForUser: List<DataRequestEntity>,
    expectedNumberOfNewlyStoredRequests: Int,
) {
    assertEquals(
        expectedNumberOfNewlyStoredRequests,
        recentlyStoredRequestsForUser.size,
        "The number of individual requests stored does not match the expected amount.",
    )
}

fun checkThatRequestForDataTypeAndIdentifierExistsExactlyOnce(
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

fun checkErrorMessageForClientException(clientException: ClientException) {
    assertEquals("Client error : 400 ", clientException.message)
}

fun checkThatRequestExistsExactlyOnceOnAggregateLevelWithCorrectCount(
    aggregatedDataRequests: List<AggregatedDataRequest>,
    framework: BulkDataRequest.ListOfFrameworkNames,
    identifierType: DataRequestCompanyIdentifierType,
    identifierValue: String,
    count: Long,
) {
    val matchingAggregatedRequests = aggregatedDataRequests.filter { aggregatedDataRequest ->
        aggregatedDataRequest.dataType == findAggregatedDataRequestDataTypeForFramework(framework) &&
            aggregatedDataRequest.dataRequestCompanyIdentifierType == identifierType &&
            aggregatedDataRequest.dataRequestCompanyIdentifierValue == identifierValue
    }
    assertEquals(
        1,
        matchingAggregatedRequests.size,
        "For the ${identifierType.value} $identifierValue and the framework ${framework.value} " +
            "there is not exactly one aggregated request as expected.",
    )
    assertEquals(
        count,
        matchingAggregatedRequests[0].count,
        "For the aggregated data request with ${identifierType.value} $identifierValue and the " +
            "framework ${framework.value} the count is not as expected.",
    )
}

fun iterateThroughIdentifiersAndFrameworksAndCheckExistenceWithCount1(
    identifierMap: Map<DataRequestCompanyIdentifierType, String>,
    frameworks: List<BulkDataRequest.ListOfFrameworkNames>,
    aggregatedDataRequests: List<AggregatedDataRequest>,
) {
    identifierMap.forEach { (identifierType, identifierValue) ->
        frameworks.forEach { framework ->
            checkThatRequestExistsExactlyOnceOnAggregateLevelWithCorrectCount(
                aggregatedDataRequests, framework, identifierType, identifierValue, 1,
            )
        }
    }
}
