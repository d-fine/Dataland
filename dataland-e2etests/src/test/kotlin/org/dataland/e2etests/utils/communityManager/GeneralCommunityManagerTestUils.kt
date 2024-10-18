package org.dataland.e2etests.utils.communityManager

import org.dataland.communitymanager.openApiClient.api.RequestControllerApi
import org.dataland.communitymanager.openApiClient.infrastructure.ClientError
import org.dataland.communitymanager.openApiClient.infrastructure.ClientException
import org.dataland.communitymanager.openApiClient.model.AggregatedDataRequest
import org.dataland.communitymanager.openApiClient.model.BulkDataRequest
import org.dataland.communitymanager.openApiClient.model.BulkDataRequestResponse
import org.dataland.communitymanager.openApiClient.model.ExtendedStoredDataRequest
import org.dataland.communitymanager.openApiClient.model.RequestStatus
import org.dataland.communitymanager.openApiClient.model.StoredDataRequestMessageObject
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.IdentifierType
import org.dataland.e2etests.BASE_PATH_TO_COMMUNITY_MANAGER
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import java.time.Instant
import java.util.UUID

private val apiAccessor = ApiAccessor()
private val jwtHelper = JwtAuthenticationHelper()
private val requestControllerApi = RequestControllerApi(BASE_PATH_TO_COMMUNITY_MANAGER)

fun retrieveTimeAndWaitOneMillisecond(): Long {
    val timestamp = Instant.now().toEpochMilli()
    Thread.sleep(1)
    return timestamp
}

fun findAggregatedDataRequestDataTypeForFramework(framework: BulkDataRequest.DataTypes): AggregatedDataRequest.DataType =
    AggregatedDataRequest.DataType.entries.find { dataType -> dataType.value == framework.value }!!

fun findRequestControllerApiDataTypeForFramework(
    framework: BulkDataRequest.DataTypes,
): RequestControllerApi.DataTypesGetAggregatedDataRequests =
    RequestControllerApi.DataTypesGetAggregatedDataRequests.entries.find { dataType ->
        dataType.value == framework.value
    }!!

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
    fun generateRandomIntegerFrom10To100ButDifferentFrom20(): Int {
        val randomInt = (10..100).random()
        return if (randomInt != 20) randomInt else generateRandomIntegerFrom10To100ButDifferentFrom20()
    }

    val digits = ('0'..'9')
    val numberOfCharacters = numberOfDigits ?: generateRandomIntegerFrom10To100ButDifferentFrom20()
    return (1..numberOfCharacters).map { digits.random() }.joinToString("")
}

fun generateMapWithOneRandomValueForEachIdentifierType(): Map<IdentifierType, String> =
    mapOf(
        IdentifierType.Lei to generateRandomLei(),
        IdentifierType.Isin to generateRandomIsin(),
        IdentifierType.PermId to generateRandomPermId(),
    )

fun getIdForUploadedCompanyWithIdentifiers(
    lei: String? = null,
    isins: List<String>? = null,
    permId: String? = null,
): String = apiAccessor.uploadOneCompanyWithIdentifiers(lei, isins, permId)!!.actualStoredCompany.companyId

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
    val totalNumberOfCompanyIdentifiers = expectedNumberOfAcceptedIdentifiers + expectedNumberOfRejectedIdentifiers
    val errorMessage = "The message sent as part of the response to the bulk data request is not as expected."
    when (expectedNumberOfRejectedIdentifiers) {
        0 ->
            assertEquals(
                "All of your $totalNumberOfCompanyIdentifiers distinct company identifiers were accepted.",
                requestResponse.message,
                errorMessage,
            )

        1 ->
            assertEquals(
                "One of your $totalNumberOfCompanyIdentifiers distinct company identifiers was rejected " +
                    "because it could not be uniquely matched with an existing company on Dataland.",
                requestResponse.message,
                errorMessage,
            )

        else ->
            assertEquals(
                "$expectedNumberOfRejectedIdentifiers of your $totalNumberOfCompanyIdentifiers distinct company " +
                    "identifiers were rejected because they could not be uniquely matched with existing " +
                    "companies on Dataland.",
                requestResponse.message,
                errorMessage,
            )
    }
}

fun checkThatTheAmountOfNewlyStoredRequestsIsAsExpected(
    recentlyStoredRequestsForUser: List<ExtendedStoredDataRequest>,
    expectedNumberOfNewlyStoredRequests: Int,
) {
    assertEquals(
        expectedNumberOfNewlyStoredRequests,
        recentlyStoredRequestsForUser.size,
        "The number of individual requests stored does not match the expected amount.",
    )
}

fun checkThatDataRequestExistsExactlyOnceInRecentlyStored(
    recentlyStoredRequestsForUser: List<ExtendedStoredDataRequest>,
    framework: String,
    reportingPeriod: String,
    dataRequestCompanyIdentifierValue: String?,
) {
    assertEquals(
        1,
        recentlyStoredRequestsForUser
            .filter { storedDataRequest ->
                storedDataRequest.dataType == framework &&
                    storedDataRequest.reportingPeriod == reportingPeriod &&
                    storedDataRequest.datalandCompanyId == dataRequestCompanyIdentifierValue
            }.size,
        "For dataland company Id $dataRequestCompanyIdentifierValue " +
            "and the framework $framework there is not exactly one newly stored request as expected.",
    )
}

fun check400ClientExceptionErrorMessage(clientException: ClientException) {
    assertEquals("Client error : 400 ", clientException.message)
}

fun checkThatRequestExistsExactlyOnceOnAggregateLevelWithCorrectCount(
    aggregatedDataRequests: List<AggregatedDataRequest>,
    framework: BulkDataRequest.DataTypes,
    reportingPeriod: String,
    identifierValue: String,
    count: Long,
) {
    val companyIdForIdentifierValue = getUniqueDatalandCompanyIdForIdentifierValue(identifierValue)
    val matchingAggregatedRequests =
        aggregatedDataRequests.filter { aggregatedDataRequest ->
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

fun iterateThroughAllThreeSpecificationsAndCheckAggregationWithCount(
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

fun assertStatusForDataRequestId(
    dataRequestId: UUID,
    expectedStatus: RequestStatus,
) {
    jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
    val retrievedStoredDataRequest = requestControllerApi.getDataRequestById(dataRequestId)
    assertEquals(expectedStatus, retrievedStoredDataRequest.requestStatus)
}

fun patchDataRequestAndAssertNewStatusAndLastModifiedUpdated(
    dataRequestId: UUID,
    newStatus: RequestStatus,
) {
    val oldLastUpdatedTimestamp = requestControllerApi.getDataRequestById(dataRequestId).lastModifiedDate
    val storedDataRequestAfterPatch = requestControllerApi.patchDataRequest(dataRequestId, newStatus)
    val newLastUpdatedTimestamp = requestControllerApi.getDataRequestById(dataRequestId).lastModifiedDate
    assertTrue(oldLastUpdatedTimestamp < newLastUpdatedTimestamp)
    assertEquals(newLastUpdatedTimestamp, storedDataRequestAfterPatch.lastModifiedDate)
    assertEquals(newStatus, storedDataRequestAfterPatch.requestStatus)
    assertStatusForDataRequestId(dataRequestId, newStatus)
}

fun generateCompaniesWithOneRandomValueForEachIdentifierType(uniqueIdentifiersMap: Map<IdentifierType, String>) {
    val baseCompany =
        CompanyInformation(
            companyName = "Name",
            headquarters = "HQ",
            identifiers =
                mapOf(
                    "Dummmy" to listOf(),
                ),
            countryCode = "DE",
        )
    jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
    for (identifierType in uniqueIdentifiersMap.keys) {
        apiAccessor.companyDataControllerApi.postCompany(
            baseCompany.copy(
                companyName = "Company${identifierType.value}",
                identifiers =
                    mapOf(
                        identifierType.value to
                            listOf(
                                "Test-${identifierType.value}${uniqueIdentifiersMap.getValue(identifierType)}",
                            ),
                    ),
            ),
        )
    }
}

fun getUniqueDatalandCompanyIdForIdentifierValue(identifierValue: String): String {
    val matchingCompanyIdsAndNamesOnDataland =
        apiAccessor.companyDataControllerApi.getCompaniesBySearchString(identifierValue)
    assertEquals(1, matchingCompanyIdsAndNamesOnDataland.size)
    return matchingCompanyIdsAndNamesOnDataland.first().companyId
}

fun getNewlyStoredRequestsAfterTimestamp(timestamp: Long): List<ExtendedStoredDataRequest> =
    requestControllerApi.getDataRequestsForRequestingUser().filter { storedDataRequest ->
        storedDataRequest.creationTimestamp > timestamp
    }

fun getMessageHistoryOfRequest(dataRequestId: String): List<StoredDataRequestMessageObject> =
    requestControllerApi
        .getDataRequestById(UUID.fromString(dataRequestId))
        .messageHistory

fun assertAccessDeniedResponseBodyInCommunityManagerClientException(communityManagerClientException: ClientException) {
    assertErrorCodeInCommunityManagerClientException(communityManagerClientException, 403)
    val responseBody = (communityManagerClientException.response as ClientError<*>).body as String
    assertTrue(responseBody.contains("Access Denied"))
}

fun assertErrorCodeInCommunityManagerClientException(
    communityManagerClientException: ClientException,
    expectedErrorCode: Number,
) {
    assertEquals("Client error : $expectedErrorCode ", communityManagerClientException.message)
}
