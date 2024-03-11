package org.dataland.e2etests.utils.communityManager

import org.dataland.communitymanager.openApiClient.api.RequestControllerApi
import org.dataland.communitymanager.openApiClient.model.RequestStatus
import org.dataland.communitymanager.openApiClient.model.SingleDataRequest
import org.dataland.communitymanager.openApiClient.model.SingleDataRequestResponse
import org.dataland.e2etests.BASE_PATH_TO_COMMUNITY_MANAGER
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.getNewlyStoredRequestsAfterTimestamp
import org.dataland.e2etests.utils.patchDataRequestAndAssertNewStatusAndLastModifiedUpdated
import org.dataland.e2etests.utils.retrieveTimeAndWaitOneMillisecond
import org.junit.jupiter.api.Assertions.assertEquals
import java.util.*

val jwtHelper = JwtAuthenticationHelper()
private val requestControllerApi = RequestControllerApi(BASE_PATH_TO_COMMUNITY_MANAGER)

private fun checkThatTheNumberOfStoredReportingPeriodsIsAsExpected(
    singleDataRequestResponse: SingleDataRequestResponse,
    expectedNumberOfStoredReportingPeriods: Int,
) {
    val actualNumberOfStoredReportingPeriods = singleDataRequestResponse.reportingPeriodsOfStoredDataRequests.size
    assertEquals(
        expectedNumberOfStoredReportingPeriods,
        actualNumberOfStoredReportingPeriods,
        "Only $actualNumberOfStoredReportingPeriods of the expected $expectedNumberOfStoredReportingPeriods " +
            "correspond to actually stored data requests.",
    )
}

private fun checkThatTheNumberOfDuplicateReportingPeriodsIsAsExpected(
    singleDataRequestResponse: SingleDataRequestResponse,
    expectedNumberOfDuplicateReportingPeriods: Int,
) {
    val actualNumberOfDuplicateReportingPeriods = singleDataRequestResponse.reportingPeriodsOfDuplicateDataRequests.size
    assertEquals(
        expectedNumberOfDuplicateReportingPeriods,
        actualNumberOfDuplicateReportingPeriods,
        "Only $actualNumberOfDuplicateReportingPeriods of the expected " +
            "$expectedNumberOfDuplicateReportingPeriods correspond to duplicate data requests.",
    )
}

private fun checkThatSingleDataRequestResponseMessageIsAsExpected(
    singleDataRequestResponse: SingleDataRequestResponse,
    expectedNumberOfStoredReportingPeriods: Int,
    expectedNumberOfDuplicateReportingPeriods: Int,
) {
    val totalNumberOfReportingPeriods =
        expectedNumberOfStoredReportingPeriods + expectedNumberOfDuplicateReportingPeriods
    val errorMessage = "The message sent as part of the response to the single data request is not as expected."
    if (totalNumberOfReportingPeriods == 1) {
        when (expectedNumberOfDuplicateReportingPeriods) {
            1 -> assertEquals(
                "Your data request was not stored, as it was already created by you before and exists on Dataland.",
                singleDataRequestResponse.message,
                errorMessage,
            )
            else -> assertEquals(
                "Your data request was stored successfully.",
                singleDataRequestResponse.message,
                errorMessage,
            )
        }
    } else {
        when (expectedNumberOfDuplicateReportingPeriods) {
            0 -> assertEquals(
                "For each of the $totalNumberOfReportingPeriods reporting periods a data request was stored.",
                singleDataRequestResponse.message,
                errorMessage,
            )
            1 -> assertEquals(
                "The request for one of your $totalNumberOfReportingPeriods reporting periods was not stored, as " +
                    "it was already created by you before and exists on Dataland.",
                singleDataRequestResponse.message,
                errorMessage,
            )
            totalNumberOfReportingPeriods -> assertEquals(
                "No data request was stored, as all reporting periods correspond to duplicate requests that were " +
                    "already created by you before and exist on Dataland.",
                singleDataRequestResponse.message,
                errorMessage,
            )
            else -> assertEquals(
                "The data requests for $expectedNumberOfDuplicateReportingPeriods of your " +
                    "$totalNumberOfReportingPeriods reporting periods were not stored, as they were already " +
                    "created by you before and exist on Dataland.",
                singleDataRequestResponse.message,
                errorMessage,
            )
        }
    }
}

fun checkThatAllReportingPeriodsAreTreatedAsExpected(
    singleDataRequestResponse: SingleDataRequestResponse,
    expectedNumberOfStoredReportingPeriods: Int,
    expectedNumberOfDuplicateReportingPeriods: Int,
) {
    checkThatTheNumberOfStoredReportingPeriodsIsAsExpected(
        singleDataRequestResponse, expectedNumberOfStoredReportingPeriods,
    )
    checkThatTheNumberOfDuplicateReportingPeriodsIsAsExpected(
        singleDataRequestResponse, expectedNumberOfDuplicateReportingPeriods,
    )
    checkThatSingleDataRequestResponseMessageIsAsExpected(
        singleDataRequestResponse, expectedNumberOfStoredReportingPeriods, expectedNumberOfDuplicateReportingPeriods,
    )
}

fun postSingleDataRequestForReportingPeriodAndUpdateStatus(
    companyIdentifier: String,
    reportingPeriod: String,
    newStatus: RequestStatus? = null,
) {
    val timestampBeforeSingleRequest = retrieveTimeAndWaitOneMillisecond()
    val response = requestControllerApi.postSingleDataRequest(
        SingleDataRequest(
            companyIdentifier = companyIdentifier,
            dataType = SingleDataRequest.DataType.lksg,
            reportingPeriods = setOf(reportingPeriod),
        ),
    )
    checkThatAllReportingPeriodsAreTreatedAsExpected(
        singleDataRequestResponse = response,
        expectedNumberOfStoredReportingPeriods = 1,
        expectedNumberOfDuplicateReportingPeriods = 0,
    )
    val dataRequestId = UUID.fromString(
        getNewlyStoredRequestsAfterTimestamp(timestampBeforeSingleRequest)[0].dataRequestId,
    )
    if (newStatus != null) {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        patchDataRequestAndAssertNewStatusAndLastModifiedUpdated(dataRequestId, newStatus)
    }
}

fun postStandardSingleDataRequest(
    companyIdentifier: String,
    contacts: Set<String>? = null,
    message: String? = null,
): SingleDataRequestResponse {
    return requestControllerApi.postSingleDataRequest(
        SingleDataRequest(
            companyIdentifier = companyIdentifier,
            dataType = SingleDataRequest.DataType.sfdr,
            reportingPeriods = setOf("2022"),
            contacts = contacts,
            message = message,
        ),
    )
}
