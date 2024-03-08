package org.dataland.e2etests.utils.communityManager

import org.dataland.communitymanager.openApiClient.model.SingleDataRequestResponse
import org.junit.jupiter.api.Assertions.assertEquals

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
