package org.dataland.e2etests.utils

import org.dataland.communitymanager.openApiClient.api.RequestControllerApi
import org.dataland.communitymanager.openApiClient.infrastructure.ClientError
import org.dataland.communitymanager.openApiClient.infrastructure.ClientException
import org.dataland.communitymanager.openApiClient.model.AggregatedDataRequest
import org.dataland.communitymanager.openApiClient.model.BulkDataRequest
import org.dataland.communitymanager.openApiClient.model.BulkDataRequestResponse
import org.dataland.communitymanager.openApiClient.model.DataRequestCompanyIdentifierType
import org.dataland.communitymanager.openApiClient.model.RequestStatus
import org.dataland.communitymanager.openApiClient.model.StoredDataRequest
import org.dataland.e2etests.BASE_PATH_TO_COMMUNITY_MANAGER
import org.dataland.e2etests.tests.communityManager.BulkDataRequestsTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.assertThrows
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.*

private val apiAccessor = ApiAccessor()

fun retrieveTimeAndWaitOneMillisecond(): Long {
    val timestamp = Instant.now().toEpochMilli()
    Thread.sleep(1)
    return timestamp
}

fun findStoredDataRequestDataTypeForFramework(
    framework: BulkDataRequest.FrameworkNames,
): StoredDataRequest.DataType {
    return StoredDataRequest.DataType.entries.find { dataType -> dataType.value == framework.value }!!
}
fun findAggregatedDataRequestDataTypeForFramework(
    framework: BulkDataRequest.FrameworkNames,
): AggregatedDataRequest.DataType {
    return AggregatedDataRequest.DataType.entries.find { dataType -> dataType.value == framework.value }!!
}
fun findRequestControllerApiDataTypeForFramework(
    framework: BulkDataRequest.FrameworkNames,
): RequestControllerApi.DataTypesGetAggregatedDataRequests {
    return RequestControllerApi.DataTypesGetAggregatedDataRequests.entries.find { dataType ->
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

fun generateMapWithOneRandomValueForEachIdentifierType(): Map<DataRequestCompanyIdentifierType, String> {
    return mapOf(
        DataRequestCompanyIdentifierType.lei to generateRandomLei(),
        DataRequestCompanyIdentifierType.isin to generateRandomIsin(),
        DataRequestCompanyIdentifierType.permId to generateRandomPermId(),
    )
}

fun getIdForUploadedCompanyWithIdentifiers(
    lei: String? = null,
    isins: List<String>? = null,
    permId: String? = null,
): String {
    return apiAccessor.uploadOneCompanyWithIdentifiers(lei, isins, permId)!!.actualStoredCompany.companyId
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
    recentlyStoredRequestsForUser: List<StoredDataRequest>,
    expectedNumberOfNewlyStoredRequests: Int,
) {
    assertEquals(
        expectedNumberOfNewlyStoredRequests,
        recentlyStoredRequestsForUser.size,
        "The number of individual requests stored does not match the expected amount.",
    )
}

fun checkThatRequestForFrameworkReportingPeriodAndIdentifierExistsExactlyOnce(
    recentlyStoredRequestsForUser: List<StoredDataRequest>,
    framework: BulkDataRequest.FrameworkNames,
    reportingPeriod: String,
    dataRequestCompanyIdentifierType: DataRequestCompanyIdentifierType,
    dataRequestCompanyIdentifierValue: String,
) {
    assertEquals(
        1,
        recentlyStoredRequestsForUser.filter { storedDataRequest ->
            storedDataRequest.dataType == findStoredDataRequestDataTypeForFramework(framework) &&
                storedDataRequest.reportingPeriod == reportingPeriod &&
                storedDataRequest.dataRequestCompanyIdentifierType == dataRequestCompanyIdentifierType &&
                storedDataRequest.dataRequestCompanyIdentifierValue == dataRequestCompanyIdentifierValue
        }.size,
        "For the ${dataRequestCompanyIdentifierType.value} $dataRequestCompanyIdentifierValue " +
            "and the framework ${framework.value} there is not exactly one newly stored request as expected.",
    )
}

fun check400ClientExceptionErrorMessage(clientException: ClientException) {
    assertEquals("Client error : 400 ", clientException.message)
}

fun causeClientExceptionByBulkDataRequest(
    listOfIdentifiers: List<String>,
    listOfFrameworks: List<BulkDataRequest.FrameworkNames>,
    listOfReportingPeriods: List<String>,
): ClientException {
    val clientException = assertThrows<ClientException> {
        RequestControllerApi(BASE_PATH_TO_COMMUNITY_MANAGER).postBulkDataRequest(
            BulkDataRequest(
                listOfIdentifiers, listOfFrameworks, listOfReportingPeriods,
            ),
        )
    }
    return clientException
}

private fun errorMessageForEmptyInputConfigurations(
    listOfIdentifiers: List<String>,
    listOfFrameworks: List<BulkDataRequest.FrameworkNames>,
    listOfReportingPeriods: List<String>,
): String {
    return when {
        listOfIdentifiers.isEmpty() && listOfFrameworks.isEmpty() && listOfReportingPeriods.isEmpty() ->
            "All " +
                "provided lists are empty."
        listOfIdentifiers.isEmpty() && listOfFrameworks.isEmpty() ->
            "The lists of company identifiers and " +
                "frameworks are empty."
        listOfIdentifiers.isEmpty() && listOfReportingPeriods.isEmpty() ->
            "The lists of company identifiers and " +
                "reporting periods are empty."
        listOfFrameworks.isEmpty() && listOfReportingPeriods.isEmpty() ->
            "The lists of frameworks and reporting " +
                "periods are empty."
        listOfIdentifiers.isEmpty() -> "The list of company identifiers is empty."
        listOfFrameworks.isEmpty() -> "The list of frameworks is empty."
        else -> "The list of reporting periods is empty."
    }
}

fun sendBulkRequestWithEmptyInputAndCheckErrorMessage(
    listOfIdentifiers: List<String>,
    listOfFrameworks: List<BulkDataRequest.FrameworkNames>,
    listOfReportingPeriods: List<String>,
) {
    val logger = LoggerFactory.getLogger(BulkDataRequestsTest::class.java)
    if (listOfIdentifiers.isNotEmpty() && listOfFrameworks.isNotEmpty() && listOfReportingPeriods.isNotEmpty()) {
        logger.info(
            "None of the input lists is empty although a function to assert the error message due to their" +
                "emptiness is called.",
        )
    } else {
        val clientException = causeClientExceptionByBulkDataRequest(
            listOfIdentifiers, listOfFrameworks, listOfReportingPeriods,
        )
        check400ClientExceptionErrorMessage(clientException)
        val responseBody = (clientException.response as ClientError<*>).body as String
        assertTrue(responseBody.contains("No empty lists are allowed as input for bulk data request."))
        assertTrue(
            responseBody.contains(
                errorMessageForEmptyInputConfigurations(listOfIdentifiers, listOfFrameworks, listOfReportingPeriods),
            ),
        )
    }
}

fun checkErrorMessageForInvalidIdentifiersInBulkRequest(clientException: ClientException) {
    check400ClientExceptionErrorMessage(clientException)
    val responseBody = (clientException.response as ClientError<*>).body as String
    assertTrue(responseBody.contains("All provided company identifiers have an invalid format."))
    assertTrue(
        responseBody.contains(
            "The company identifiers you provided do not match the patterns of a valid LEI, ISIN or PermId.",
        ),
    )
}

fun checkThatRequestExistsExactlyOnceOnAggregateLevelWithCorrectCount(
    aggregatedDataRequests: List<AggregatedDataRequest>,
    framework: BulkDataRequest.FrameworkNames,
    reportingPeriod: String,
    identifierType: DataRequestCompanyIdentifierType,
    identifierValue: String,
    count: Long,
) {
    val matchingAggregatedRequests = aggregatedDataRequests.filter { aggregatedDataRequest ->
        aggregatedDataRequest.dataType == findAggregatedDataRequestDataTypeForFramework(framework) &&
            aggregatedDataRequest.reportingPeriod == reportingPeriod &&
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

fun iterateThroughFrameworksReportingPeriodsAndIdentifiersAndCheckAggregationWithCount(
    aggregatedDataRequests: List<AggregatedDataRequest>,
    frameworks: List<BulkDataRequest.FrameworkNames>,
    reportingPeriods: List<String>,
    identifierMap: Map<DataRequestCompanyIdentifierType, String>,
    count: Long,
) {
    frameworks.forEach { framework ->
        reportingPeriods.forEach { reportingPeriod ->
            identifierMap.forEach { (identifierType, identifierValue) ->
                checkThatRequestExistsExactlyOnceOnAggregateLevelWithCorrectCount(
                    aggregatedDataRequests, framework, reportingPeriod, identifierType, identifierValue, count,
                )
            }
        }
    }
}

fun assertStatusForDataRequestId(dataRequestId: UUID, expectedStatus: RequestStatus) {
    val retrievedStoredDataRequest = RequestControllerApi(BASE_PATH_TO_COMMUNITY_MANAGER)
        .getDataRequestById(dataRequestId)
    assertEquals(expectedStatus, retrievedStoredDataRequest.requestStatus)
}

fun patchDataRequestAndAssertNewStatus(dataRequestId: UUID, newStatus: RequestStatus) {
    val storedDataRequestAfterPatch = RequestControllerApi(BASE_PATH_TO_COMMUNITY_MANAGER)
        .patchDataRequest(dataRequestId, newStatus)
    assertEquals(newStatus, storedDataRequestAfterPatch.requestStatus)
    assertStatusForDataRequestId(dataRequestId, newStatus)
}
