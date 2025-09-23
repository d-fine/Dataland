package org.dataland.datalandbackendutils.services

import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneId

@Service("CommonDataRequestProcessingUtils")
class CommonDataRequestProcessingUtils {
    fun getEpochTimeStartOfDay(): Long {
        val instantNow = Instant.ofEpochMilli(System.currentTimeMillis())
        val zoneId = ZoneId.of("Europe/Berlin")
        val instantNowZoned = instantNow.atZone(zoneId)
        val startOfDay = instantNowZoned.toLocalDate().atStartOfDay(zoneId)
        return startOfDay.toInstant().toEpochMilli()
    }

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
