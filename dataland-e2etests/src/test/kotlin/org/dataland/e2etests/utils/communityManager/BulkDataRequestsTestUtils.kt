package org.dataland.e2etests.utils.communityManager

import org.dataland.communitymanager.openApiClient.api.RequestControllerApi
import org.dataland.communitymanager.openApiClient.infrastructure.ClientError
import org.dataland.communitymanager.openApiClient.infrastructure.ClientException
import org.dataland.communitymanager.openApiClient.model.BulkDataRequest
import org.dataland.communitymanager.openApiClient.model.BulkDataRequestResponse
import org.dataland.communitymanager.openApiClient.model.ExtendedStoredDataRequest
import org.dataland.communitymanager.openApiClient.model.RequestStatus
import org.dataland.e2etests.BASE_PATH_TO_COMMUNITY_MANAGER
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.tests.communityManager.BulkDataRequestsTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.assertThrows
import org.slf4j.LoggerFactory
import java.util.UUID

private val jwtHelper = JwtAuthenticationHelper()
private val requestControllerApi = RequestControllerApi(BASE_PATH_TO_COMMUNITY_MANAGER)

fun checkThatTheNumberOfAcceptedIdentifiersIsAsExpected(
    requestResponse: BulkDataRequestResponse,
    expectedNumberOfAcceptedIdentifiers: Int,
) {
    Assertions.assertEquals(
        expectedNumberOfAcceptedIdentifiers,
        requestResponse.acceptedCompanyIdentifiers.size,
        "Not every combination of identifier and framework was sent as a request as expected.",
    )
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

fun causeClientExceptionByBulkDataRequest(
    identifiers: Set<String>,
    dataTypes: Set<BulkDataRequest.DataTypes>,
    reportingPeriods: Set<String>,
): ClientException {
    val clientException =
        assertThrows<ClientException> {
            requestControllerApi.postBulkDataRequest(
                BulkDataRequest(
                    identifiers, dataTypes, reportingPeriods,
                ),
            )
        }
    return clientException
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
        val clientException =
            causeClientExceptionByBulkDataRequest(
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

private fun errorMessageForEmptyInputConfigurations(
    identifiers: Set<String>,
    dataTypes: Set<BulkDataRequest.DataTypes>,
    reportingPeriods: Set<String>,
): String =
    when {
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
            "The company identifiers you provided could not be uniquely matched with an existing company on dataland",
        ),
    )
}

fun retrieveDataRequestIdForReportingPeriodAndUpdateStatus(
    dataRequests: List<ExtendedStoredDataRequest>,
    reportingPeriod: String,
    newStatus: RequestStatus,
) {
    val dataRequestsForReportingPeriod = dataRequests.filter { it.reportingPeriod == reportingPeriod }
    Assertions.assertEquals(
        1,
        dataRequestsForReportingPeriod.size,
        "There is more than one data request for reporting period $reportingPeriod although it shouldn't.",
    )
    val dataRequestId = UUID.fromString(dataRequestsForReportingPeriod[0].dataRequestId)
    jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
    patchDataRequestAndAssertNewStatusAndLastModifiedUpdated(dataRequestId, newStatus)
}
