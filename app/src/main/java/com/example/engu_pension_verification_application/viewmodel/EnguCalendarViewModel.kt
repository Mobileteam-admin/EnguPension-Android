package com.example.engu_pension_verification_application.viewmodel

import androidx.lifecycle.ViewModel
import java.util.Calendar

class EnguCalendarViewModel : ViewModel() {
    enum class DayType { SELECTABLE, INVALID, HOLIDAY }

    val calendar: Calendar = Calendar.getInstance()
    var selectedDateRow = -1
    var selectedDateColumn = -1

    var selectedDay = -1
    var selectedMonth = -1
    var selectedYear = -1

    fun getSelectedDate(): Calendar? {
        if (selectedDay == -1 || selectedMonth == -1 ||  selectedYear == -1)
            return null
        return Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, selectedDay)
            set(Calendar.MONTH, selectedMonth)
            set(Calendar.YEAR, selectedYear)
        }
    }
}