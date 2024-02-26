package org.dataland.e2etests.utils

import org.dataland.communitymanager.openApiClient.api.RequestControllerApi
import org.dataland.communitymanager.openApiClient.infrastructure.ClientError
import org.dataland.communitymanager.openApiClient.infrastructure.ClientException
import org.dataland.communitymanager.openApiClient.model.AggregatedDataRequest
import org.dataland.communitymanager.openApiClient.model.BulkDataRequest
import org.dataland.communitymanager.openApiClient.model.BulkDataRequestResponse
import org.dataland.communitymanager.openApiClient.model.RequestStatus
import org.dataland.communitymanager.openApiClient.model.StoredDataRequest
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.IdentifierType
import org.dataland.e2etests.BASE_PATH_TO_COMMUNITY_MANAGER
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.tests.communityManager.BulkDataRequestsTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.assertThrows
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.*

private val apiAccessor = ApiAccessor()
val jwtHelper = JwtAuthenticationHelper()

fun retrieveTimeAndWaitOneMillisecond(): Long {
    val timestamp = Instant.now().toEpochMilli()
    Thread.sleep(1)
    return timestamp
}

fun findStoredDataRequestDataTypeForFramework(
    framework: BulkDataRequest.DataTypes,
): StoredDataRequest.DataType {
    return StoredDataRequest.DataType.entries.find { dataType -> dataType.value == framework.value }!!
}
fun findAggregatedDataRequestDataTypeForFramework(
    framework: BulkDataRequest.DataTypes,
): AggregatedDataRequest.DataType {
    return AggregatedDataRequest.DataType.entries.find { dataType -> dataType.value == framework.value }!!
}
fun findRequestControllerApiDataTypeForFramework(
    framework: BulkDataRequest.DataTypes,
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

fun generateMapWithOneRandomValueForEachIdentifierType(): Map<IdentifierType, String> {
    return mapOf(
        IdentifierType.lei to generateRandomLei(),
        IdentifierType.isin to generateRandomIsin(),
        IdentifierType.permId to generateRandomPermId(),
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
                "identifiers were rejected because they could not be matched with an existing company on dataland.",
            requestResponse.message,
            errorMessage,
        )
    }
}

fun checkThatAllIdentifiersWereAccepted(
    requestResponse: BulkDataRequestResponse,
    expectedNumberOfAcceptedIdentifiers: Int,
    expectedNumberOfRejectedIdentifiers: Int,
) {
    checkThatTheNumberOfAcceptedIdentifiersIsAsExpected(requestResponse, expectedNumberOfAcceptedIdentifiers)
    checkThatTheNumberOfRejectedIdentifiersIsAsExpected(requestResponse, expectedNumberOfRejectedIdentifiers)
    checkThatMessageIsAsExpected(
        requestResponse, expectedNumberOfAcceptedIdentifiers,
        expectedNumberOfRejectedIdentifiers,
    )
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
    framework: BulkDataRequest.DataTypes,
    reportingPeriod: String,
    dataRequestCompanyIdentifierValue: String?,
) {
    assertEquals(
        1,
        recentlyStoredRequestsForUser.filter { storedDataRequest ->
            storedDataRequest.dataType == findStoredDataRequestDataTypeForFramework(framework) &&
                storedDataRequest.reportingPeriod == reportingPeriod &&
                storedDataRequest.datalandCompanyId == dataRequestCompanyIdentifierValue
        }.size,
        "For dataland company Id $dataRequestCompanyIdentifierValue " +
            "and the framework ${framework.value} there is not exactly one newly stored request as expected.",
    )
}

fun check400ClientExceptionErrorMessage(clientException: ClientException) {
    assertEquals("Client error : 400 ", clientException.message)
}

fun causeClientExceptionByBulkDataRequest(
    identifiers: Set<String>,
    dataTypes: Set<BulkDataRequest.DataTypes>,
    reportingPeriods: Set<String>,
): ClientException {
    val clientException = assertThrows<ClientException> {
        RequestControllerApi(BASE_PATH_TO_COMMUNITY_MANAGER).postBulkDataRequest(
            BulkDataRequest(
                identifiers, dataTypes, reportingPeriods,
            ),
        )
    }
    return clientException
}

private fun errorMessageForEmptyInputConfigurations(
    identifiers: Set<String>,
    dataTypes: Set<BulkDataRequest.DataTypes>,
    reportingPeriods: Set<String>,
): String {
    return when {
        identifiers.isEmpty() && dataTypes.isEmpty() && reportingPeriods.isEmpty() ->
            "All " +
                "provided lists are empty."
        identifiers.isEmpty() && dataTypes.isEmpty() ->
            "The lists of company identifiers and " +
                "frameworks are empty."
        identifiers.isEmpty() && reportingPeriods.isEmpty() ->
            "The lists of company identifiers and " +
                "reporting periods are empty."
        dataTypes.isEmpty() && reportingPeriods.isEmpty() ->
            "The lists of frameworks and reporting " +
                "periods are empty."
        identifiers.isEmpty() -> "The list of company identifiers is empty."
        dataTypes.isEmpty() -> "The list of frameworks is empty."
        else -> "The list of reporting periods is empty."
    }
}

fun sendBulkRequestWithEmptyInputAndCheckErrorMessage(
    identifiers: Set<String>,
    dataTypes: Set<BulkDataRequest.DataTypes>,
    reportingPeriods: Set<String>,
) {
    val logger = LoggerFactory.getLogger(BulkDataRequestsTest::class.java)
    if (identifiers.isNotEmpty() && dataTypes.isNotEmpty() && reportingPeriods.isNotEmpty()) {
        logger.info(
            "None of the input lists is empty although a function to assert the error message due to their" +
                "emptiness is called.",
        )
    } else {
        val clientException = causeClientExceptionByBulkDataRequest(
            identifiers, dataTypes, reportingPeriods,
        )
        check400ClientExceptionErrorMessage(clientException)
        val responseBody = (clientException.response as ClientError<*>).body as String
        assertTrue(responseBody.contains("No empty lists are allowed as input for bulk data request."))
        assertTrue(
            responseBody.contains(
                errorMessageForEmptyInputConfigurations(identifiers, dataTypes, reportingPeriods),
            ),
        )
    }
}

fun checkErrorMessageForInvalidIdentifiersInBulkRequest(clientException: ClientException) {
    check400ClientExceptionErrorMessage(clientException)
    val responseBody = (clientException.response as ClientError<*>).body as String
    assertTrue(
        responseBody.contains(
            "All provided company identifiers are not unique or could not be " +
                "recognized.",
        ),
    )
    assertTrue(
        responseBody.contains(
            "The company identifiers you provided could not be matched with an existing company on dataland",
        ),
    )
}

fun checkThatRequestExistsExactlyOnceOnAggregateLevelWithCorrectCount(
    aggregatedDataRequests: List<AggregatedDataRequest>,
    framework: BulkDataRequest.DataTypes,
    reportingPeriod: String,
    identifierValue: String,
    count: Long,
) {
    val companyIdForIdentifierValue = getUniqueDatalandCompanyIdForIdentifierValue(identifierValue)
    val matchingAggregatedRequests = aggregatedDataRequests.filter { aggregatedDataRequest ->
        aggregatedDataRequest.dataType == findAggregatedDataRequestDataTypeForFramework(framework) &&
            aggregatedDataRequest.reportingPeriod == reportingPeriod &&
            aggregatedDataRequest.datalandCompanyId == companyIdForIdentifierValue
    }
    assertEquals(
        1,
        matchingAggregatedRequests.size,
        "For the $identifierValue and the framework ${framework.value} " +
            "there is not exactly one aggregated request as expected.",
    )
    assertEquals(
        count,
        matchingAggregatedRequests[0].count,
        "For the aggregated data request with  $identifierValue and the " +
            "framework ${framework.value} the count is not as expected.",
    )
}

fun iterateThroughFrameworksReportingPeriodsAndIdentifiersAndCheckAggregationWithCount(
    aggregatedDataRequests: List<AggregatedDataRequest>,
    frameworks: Set<BulkDataRequest.DataTypes>,
    reportingPeriods: Set<String>,
    identifiers: Set<String>,
    count: Long,
) {
    frameworks.forEach { framework ->
        reportingPeriods.forEach { reportingPeriod ->
            identifiers.forEach { identifier ->
                checkThatRequestExistsExactlyOnceOnAggregateLevelWithCorrectCount(
                    aggregatedDataRequests, framework, reportingPeriod, identifier, count,
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

fun patchDataRequestAndAssertNewStatusAndLastModifiedUpdated(dataRequestId: UUID, newStatus: RequestStatus) {
    val requestControllerApi = RequestControllerApi(BASE_PATH_TO_COMMUNITY_MANAGER)
    val oldLastUpdatedTimestamp = requestControllerApi.getDataRequestById(dataRequestId).lastModifiedDate
    val storedDataRequestAfterPatch = requestControllerApi.patchDataRequestStatus(dataRequestId, newStatus)
    val newLastUpdatedTimestamp = requestControllerApi.getDataRequestById(dataRequestId).lastModifiedDate
    assertTrue(oldLastUpdatedTimestamp < newLastUpdatedTimestamp)
    assertEquals(newLastUpdatedTimestamp, storedDataRequestAfterPatch.lastModifiedDate)
    assertEquals(newStatus, storedDataRequestAfterPatch.requestStatus)
    assertStatusForDataRequestId(dataRequestId, newStatus)
}

fun generateCompaniesWithOneRandomValueForEachIdentifierType(
    uniqueIdentifiersMap: Map<IdentifierType, String>,
) {
    val companyZero = CompanyInformation(
        companyName = "Name",
        headquarters = "HQ",
        identifiers = mapOf(
            "Dummmy" to listOf(),
        ),
        countryCode = "DE",
    )
    jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
    for (key in uniqueIdentifiersMap.keys) {
        when {
            (key == IdentifierType.lei) -> {
                val companyOne = companyZero.copy(
                    companyName = "companyOne",
                    identifiers = mapOf(
                        key.value to listOf(
                            "Test-Lei${uniqueIdentifiersMap.getValue(IdentifierType.lei)}",
                        ),
                    ),
                )
                apiAccessor.companyDataControllerApi.postCompany(companyOne)
            }

            (key == IdentifierType.isin) -> {
                val companyTwo = companyZero.copy(
                    companyName = "companyTwo",
                    identifiers = mapOf(
                        key.value to listOf(
                            "Test-Isin${
                                uniqueIdentifiersMap.getValue(IdentifierType.isin)
                            }",
                        ),
                    ),
                )
                apiAccessor.companyDataControllerApi.postCompany(companyTwo)
            }

            (key == IdentifierType.permId) -> {
                val companyThree = companyZero.copy(
                    companyName = "companyThree",
                    identifiers = mapOf(
                        key.value to listOf(
                            "Test-PermId${uniqueIdentifiersMap.getValue(IdentifierType.permId)}",
                        ),
                    ),
                )
                apiAccessor.companyDataControllerApi.postCompany(companyThree)
            } else ->
                "The provided IdentifierType has not been implemented in the " +
                    "generateCompaniesWithOneRandomValueForEachIdentifierType function"
        }
    }
}

fun getUniqueDatalandCompanyIdForIdentifierValue(identifierValue: String): String {
    val matchingCompanyIdsAndNamesOnDataland =
        apiAccessor.companyDataControllerApi.getCompaniesBySearchString(identifierValue)
    assertEquals(1, matchingCompanyIdsAndNamesOnDataland.size)
    return matchingCompanyIdsAndNamesOnDataland.first().companyId
}
