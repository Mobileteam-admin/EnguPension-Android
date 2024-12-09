package com.example.engu_pension_verification_application.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


object CalendarUtils {
    const val DATE_FORMAT_1 = "yyyy-MM-dd"
    const val DATE_FORMAT_2 = "MMMM yyyy"
    const val DATE_FORMAT_3 = "dd/MM/yyyy"
    const val DATE_TIME_FORMAT_1 = "yyyy-MM-dd HH:mm:ss" //"2024-12-09 05:42:11"
    fun getFormattedString(
        currentFormat: String,
        requiredFormat: String,
        dateTimeString: String
    ): String {
        val calendar = getCalendar(currentFormat, dateTimeString)
        return if (calendar == null) "" else getFormattedString(requiredFormat, calendar)
    }

    fun getFormattedString(format: String, calendar: Calendar): String {
        try {
            return SimpleDateFormat(format, Locale.getDefault()).format(calendar.time)
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    fun getMonthNum(monthName: String): Int? { //monthName in the format -> 'January', 'February',...
        val date: Date? = SimpleDateFormat("MMMM", Locale.ENGLISH).parse(monthName)
        val cal: Calendar = Calendar.getInstance()
        if (date != null) {
            cal.time = date
            return cal.get(Calendar.MONTH)
        }
        return null
    }

    fun getCalendar(format: String, dateString: String): Calendar? {
        val dateFormat = SimpleDateFormat(format, Locale.getDefault())
        return try {
            val date = dateFormat.parse(dateString)
            Calendar.getInstance().apply {
                time = date!!
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun isSameDay(date1: Calendar?, date2: Calendar?): Boolean {
        if (date1 == null || date2 == null) return false
        return date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR) &&
                date1.get(Calendar.MONTH) == date2.get(Calendar.MONTH) &&
                date1.get(Calendar.DAY_OF_MONTH) == date2.get(Calendar.DAY_OF_MONTH)
    }

    fun isDateInRange(date: Calendar, startDate: Calendar, endDate: Calendar): Boolean {
        val actualStartDate = if (startDate.before(endDate)) startDate else endDate
        val actualEndDate = if (startDate.before(endDate)) endDate else startDate
        setDayBegin(actualStartDate)
        setDayEnd(actualEndDate)
        return !(date.before(actualStartDate) || date.after(actualEndDate))
    }

    fun setDayBegin(calendar: Calendar) {
        calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    fun setDayEnd(calendar: Calendar) {
        val tempCalendar = Calendar.getInstance()
        calendar.apply {
            set(Calendar.HOUR_OF_DAY, tempCalendar.getActualMaximum(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, tempCalendar.getActualMaximum(Calendar.MINUTE))
            set(Calendar.SECOND, tempCalendar.getActualMaximum(Calendar.SECOND))
            set(Calendar.MILLISECOND, tempCalendar.getActualMaximum(Calendar.MILLISECOND))
        }
    }
}