package org.dataland.datalandbackendutils.utils

import java.time.Instant
import java.time.ZoneId

/**
 * Utility functions for common data request processing.
 */
object CommonDataRequestProcessingUtils {
    /**
     * Returns the epoch time in milliseconds for the start of the current day in the "Europe/Berlin" timezone.
     */
    fun getEpochTimeStartOfDay(): Long {
        val instantNow = Instant.ofEpochMilli(System.currentTimeMillis())
        val zoneId = ZoneId.of("Europe/Berlin")
        val instantNowZoned = instantNow.atZone(zoneId)
        val startOfDay = instantNowZoned.toLocalDate().atStartOfDay(zoneId)
        return startOfDay.toInstant().toEpochMilli()
    }

    /**
     * Builds a response message for a single data request based on the total number of reporting periods
     * and the number of reporting periods corresponding to duplicate requests.
     *
     * @param totalNumberOfReportingPeriods The total number of reporting periods in the request.
     * @param numberOfReportingPeriodsCorrespondingToDuplicates The number of reporting periods that correspond to duplicate requests.
     * @return A response message as a String.
     */
    fun buildResponseMessageForSingleDataRequest(
        totalNumberOfReportingPeriods: Int,
        numberOfReportingPeriodsCorrespondingToDuplicates: Int,
    ): String =
        if (totalNumberOfReportingPeriods == 1) {
            when (numberOfReportingPeriodsCorrespondingToDuplicates) {
                1 -> "Your data request was not stored, as it was already created by you before and exists on Dataland."
                else -> "Your data request was stored successfully."
            }
        } else {
            when (numberOfReportingPeriodsCorrespondingToDuplicates) {
                0 -> "For each of the $totalNumberOfReportingPeriods reporting periods a data request was stored."
                1 ->
                    "The request for one of your $totalNumberOfReportingPeriods reporting periods was not stored, as " +
                        "it was already created by you before and exists on Dataland."

                totalNumberOfReportingPeriods ->
                    "No data request was stored, as all reporting periods correspond to duplicate requests that were " +
                        "already created by you before and exist on Dataland."

                else ->
                    "The data requests for $numberOfReportingPeriodsCorrespondingToDuplicates of your " +
                        "$totalNumberOfReportingPeriods reporting periods were not stored, as they were already " +
                        "created by you before and exist on Dataland."
            }
        }
}
