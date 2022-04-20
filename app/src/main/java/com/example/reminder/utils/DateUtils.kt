package com.example.reminder.utils

import java.text.SimpleDateFormat
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

object DateUtils {

    private var dateFormat: SimpleDateFormat =
        SimpleDateFormat("EEE, MMM dd yyyy", Locale.getDefault())
    private const val timeFormat: String = "%02d:%02d"

    fun formatDate(date: Date): String = dateFormat.format(date)

    fun parseDate(date: String): Date? = dateFormat.parse(date)

    fun formatTime(hour: Int, min: Int) = String.format(timeFormat, hour, min)

    fun parseTime(time: String) = time.split(":")

    fun isToday(date: String): Boolean? {
        val formattedDate = Calendar.getInstance()
        parseDate(date)?.let {
            formattedDate.time = it
            val calendar = Calendar.getInstance()
            return formattedDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) && formattedDate.get(
                Calendar.MONTH
            ) == calendar.get(Calendar.MONTH) && formattedDate.get(Calendar.DAY_OF_MONTH) == calendar.get(
                Calendar.DAY_OF_MONTH
            )
        }
        return null
    }

    fun isOlderDate(date: Date?): Boolean? {
        date?.let {
            val formattedDate = Calendar.getInstance()
            formattedDate.time = date

            val calendar = Calendar.getInstance()

            if (formattedDate.get(Calendar.YEAR) < calendar.get(Calendar.YEAR))
                return true
            else if (formattedDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) {
                if (formattedDate.get(Calendar.MONTH) < calendar.get(Calendar.MONTH))
                    return true
                else if (formattedDate.get(Calendar.MONTH) == calendar.get(Calendar.MONTH))
                    return formattedDate.get(Calendar.DAY_OF_MONTH) < calendar.get(Calendar.DAY_OF_MONTH)
            }
        }
        return null
    }

    fun getDateOrDay(date: String): String {
        val formattedDate = Calendar.getInstance()
        var dayOrDate: String = ""
        parseDate(date)?.let {
            formattedDate.time = it
            val calendar = Calendar.getInstance()

            val currentYear = calendar.get(Calendar.YEAR)
            val year = formattedDate.get(Calendar.YEAR)

            val currentMonth = calendar.get(Calendar.MONTH)
            val month = formattedDate.get(Calendar.MONTH)

            val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
            val day = formattedDate.get(Calendar.DAY_OF_MONTH)

            when (year) {
                currentYear -> {
                    dayOrDate = if (month == currentMonth) {
                        when (day - currentDay) {
                            0 -> "Today"
                            1 -> "Tomorrow"
                            else -> date
                        }
                    } else if (month == currentMonth + 1) {
                        val yearMonth = YearMonth.parse(formatDate(calendar.time), DateTimeFormatter.ofPattern(dateFormat.toPattern()))
                        val endMonth = yearMonth.lengthOfMonth()
                        if (currentDay == endMonth && day == 1)
                            "Tomorrow"
                        else
                            date
                    } else {
                        date
                    }
                }
                currentYear + 1 -> {
                    dayOrDate = if (currentMonth == 11 && month == 0 && currentDay == 31 && day == 1)
                        "Tomorrow"
                    else
                        date
                }
                else -> {
                    dayOrDate = date
                }
            }
        }
        return dayOrDate
    }
}