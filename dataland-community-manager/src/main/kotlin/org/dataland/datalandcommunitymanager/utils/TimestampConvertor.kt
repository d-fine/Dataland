package org.dataland.datalandcommunitymanager.utils

import java.time.Instant
import java.time.ZoneId

/**
 * Class provides functions to manipulate timestamps.
 */
class TimestampConvertor {

    /** This method returns, for a specified timestamp, a timestamp
     * representing the beginning of that day, say, midnight.
     * @param timestampMillisNow representing current time
     * @returns the timestamp for the beginning of the day
     */
    fun getTimestampStartOfDay(timestampMillisNow: Long): Long {
        val instantNow = Instant.ofEpochMilli(timestampMillisNow)
        val zoneId = ZoneId.of("Europe/Berlin")
        val instantNowZoned = instantNow.atZone(zoneId)
        val startOfDay = instantNowZoned.toLocalDate().atStartOfDay(zoneId)
        val startOfDayTimestampMillis = startOfDay.toInstant().toEpochMilli()
        return startOfDayTimestampMillis
    }
}
