package org.dataland.e2etests.utils.communityManager

import org.dataland.communitymanager.openApiClient.api.RequestControllerApi
import org.dataland.communitymanager.openApiClient.infrastructure.ClientError
import org.dataland.communitymanager.openApiClient.infrastructure.ClientException
import org.dataland.communitymanager.openApiClient.model.AggregatedDataRequestWithAggregatedPriority
import org.dataland.communitymanager.openApiClient.model.BulkDataRequest
import org.dataland.communitymanager.openApiClient.model.BulkDataRequestResponse
import org.dataland.communitymanager.openApiClient.model.DataRequestPatch
import org.dataland.communitymanager.openApiClient.model.ExtendedStoredDataRequest
import org.dataland.communitymanager.openApiClient.model.RequestPriority
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

fun findAggregatedDataRequestDataTypeForFramework(
    framework: BulkDataRequest.DataTypes,
): AggregatedDataRequestWithAggregatedPriority.DataType =
    AggregatedDataRequestWithAggregatedPriority.DataType.entries.find { dataType -> dataType.value == framework.value }!!

fun findRequestControllerApiDataTypeForFramework(
    framework: BulkDataRequest.DataTypes,
): RequestControllerApi.DataTypesGetAggregatedOpenDataRequests =
    RequestControllerApi.DataTypesGetAggregatedOpenDataRequests.entries.find { dataType ->
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

fun checkThatTheNumberOfRejectedCompanyIdentifiersIsAsExpected(
    requestResponse: BulkDataRequestResponse,
    expectedNumberOfRejectedIdentifiers: Int,
) {
    assertEquals(
        expectedNumberOfRejectedIdentifiers,
        requestResponse.rejectedCompanyIdentifiers.size,
        "Not all identifiers were accepted as expected.",
    )
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

fun checkClientExceptionErrorMessage(
    clientException: ClientException,
    errorCode: Int? = 400,
) {
    assertEquals(errorCode, clientException.statusCode)
    assertEquals("Client error : $errorCode ", clientException.message)
}

fun checkThatRequestExistsExactlyOnceOnAggregateLevelWithCorrectCount(
    aggregatedDataRequestsWithAggregatedPriority: List<AggregatedDataRequestWithAggregatedPriority>,
    framework: BulkDataRequest.DataTypes,
    reportingPeriod: String,
    identifierValue: String,
    count: Long,
) {
    val companyIdForIdentifierValue = getUniqueDatalandCompanyIdForIdentifierValue(identifierValue)
    val matchingAggregatedRequests =
        aggregatedDataRequestsWithAggregatedPriority.filter { aggregatedDataRequestWithAggregatedPriority ->
            aggregatedDataRequestWithAggregatedPriority.dataType == findAggregatedDataRequestDataTypeForFramework(framework) &&
                aggregatedDataRequestWithAggregatedPriority.reportingPeriod == reportingPeriod &&
                aggregatedDataRequestWithAggregatedPriority.datalandCompanyId == companyIdForIdentifierValue
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
    aggregatedDataRequestWithAggregatedPriority: List<AggregatedDataRequestWithAggregatedPriority>,
    frameworks: Set<BulkDataRequest.DataTypes>,
    reportingPeriods: Set<String>,
    identifiers: Set<String>,
    count: Long,
) {
    frameworks.forEach { framework ->
        reportingPeriods.forEach { reportingPeriod ->
            identifiers.forEach { identifier ->
                checkThatRequestExistsExactlyOnceOnAggregateLevelWithCorrectCount(
                    aggregatedDataRequestWithAggregatedPriority, framework, reportingPeriod, identifier, count,
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

fun assertPriorityForDataRequestId(
    dataRequestId: UUID,
    expectedPriority: RequestPriority,
) {
    jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
    val retrievedStoredDataRequest = requestControllerApi.getDataRequestById(dataRequestId)
    assertEquals(expectedPriority, retrievedStoredDataRequest.requestPriority)
}

fun assertAdminCommentForDataRequestId(
    dataRequestId: UUID,
    expectedAdminComment: String?,
) {
    jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
    val retrievedStoredDataRequest = requestControllerApi.getDataRequestById(dataRequestId)
    assertEquals(expectedAdminComment, retrievedStoredDataRequest.adminComment)
}

fun patchDataRequestAndAssertNewStatusAndLastModifiedUpdated(
    dataRequestId: UUID,
    newStatus: RequestStatus,
) {
    val oldLastUpdatedTimestamp = requestControllerApi.getDataRequestById(dataRequestId).lastModifiedDate
    val statusDataRequestPatch = DataRequestPatch(requestStatus = newStatus)
    val storedDataRequestAfterPatch = requestControllerApi.patchDataRequest(dataRequestId, statusDataRequestPatch)
    val newLastUpdatedTimestamp = requestControllerApi.getDataRequestById(dataRequestId).lastModifiedDate
    assertTrue(oldLastUpdatedTimestamp < newLastUpdatedTimestamp)
    assertEquals(newLastUpdatedTimestamp, storedDataRequestAfterPatch.lastModifiedDate)
    assertEquals(newStatus, storedDataRequestAfterPatch.requestStatus)
    assertStatusForDataRequestId(dataRequestId, newStatus)
}

fun patchDataRequestAdminCommentAndAssertLastModifiedNotUpdated(
    dataRequestId: UUID,
    newAdminComment: String,
) {
    val oldLastUpdatedTimestamp = requestControllerApi.getDataRequestById(dataRequestId).lastModifiedDate
    val adminCommentDataRequestPatch = DataRequestPatch(adminComment = newAdminComment)
    val storedDataRequestAfterPatch = requestControllerApi.patchDataRequest(dataRequestId, adminCommentDataRequestPatch)
    val newLastUpdatedTimestamp = requestControllerApi.getDataRequestById(dataRequestId).lastModifiedDate
    assertTrue(oldLastUpdatedTimestamp == newLastUpdatedTimestamp)
    assertEquals(newAdminComment, storedDataRequestAfterPatch.adminComment)
    assertAdminCommentForDataRequestId(dataRequestId, newAdminComment)
}

fun patchDataRequestPriorityAndAssertLastModifiedUpdated(
    dataRequestId: UUID,
    newRequestPriority: RequestPriority,
) {
    val oldLastUpdatedTimestamp = requestControllerApi.getDataRequestById(dataRequestId).lastModifiedDate
    val priorityDataRequestPatch = DataRequestPatch(requestPriority = newRequestPriority)
    val storedDataRequestAfterPatch = requestControllerApi.patchDataRequest(dataRequestId, priorityDataRequestPatch)
    val newLastUpdatedTimestamp = requestControllerApi.getDataRequestById(dataRequestId).lastModifiedDate
    assertTrue(oldLastUpdatedTimestamp < newLastUpdatedTimestamp)
    assertEquals(newRequestPriority, storedDataRequestAfterPatch.requestPriority)
    assertPriorityForDataRequestId(dataRequestId, newRequestPriority)
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
                identifiers = mapOf(identifierType.value to listOf(uniqueIdentifiersMap.getValue(identifierType))),
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

fun getUsersStoredRequestWithLatestCreationTime(): ExtendedStoredDataRequest =
    requestControllerApi.getDataRequestsForRequestingUser().maxBy { it.creationTimestamp }

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
