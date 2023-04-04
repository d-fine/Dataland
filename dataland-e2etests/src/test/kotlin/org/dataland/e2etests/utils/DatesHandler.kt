package org.dataland.e2etests.utils

import java.text.SimpleDateFormat
import java.util.*

class DatesHandler {

    private fun addDaysToDate(date: Date, days: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.DATE, days)
        return calendar.time
    }

    private fun formatDateAsSimpleDateString(date: Date): String {
        return SimpleDateFormat("MM/dd/yyyy").format(date)
    }

    fun calculateExpectedExpiryDateSimpleFormatted(daysValid: Int? = null): String? {
        return when (daysValid) {
            null -> null
            else -> formatDateAsSimpleDateString(addDaysToDate(Date(), daysValid))
        }
    }

    fun convertUnixTimeToSimpleFormattedDate(unixTimeInMilliseconds: Long? = null): String? {
        return when (unixTimeInMilliseconds) {
            null -> null
            else -> formatDateAsSimpleDateString(Date(unixTimeInMilliseconds))
        }
    }
}
