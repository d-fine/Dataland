package org.dataland.e2etests.utils

import java.text.SimpleDateFormat
import java.util.*

class DatesHandler {

    fun addDaysToDate(date: Date, days: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.DATE, days)
        return calendar.time
    }

    fun formatDateAsSimpleDateString(date: Date): String {
        return SimpleDateFormat("MM/dd/yyyy").format(date)
    }
}
