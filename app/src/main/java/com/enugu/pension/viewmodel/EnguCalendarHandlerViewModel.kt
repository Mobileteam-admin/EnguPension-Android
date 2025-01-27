package com.enugu.pension.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.enugu.pension.model.dto.EnguCalendarRange
import com.enugu.pension.util.CalendarUtils
import java.util.Calendar

class EnguCalendarHandlerViewModel : ViewModel() {
    var actionId = 1
    var initSelectedDay: Calendar? = null
    var openRangeLastMonth = true
    val onDateSelect = MutableLiveData<Calendar?>(null)
    var enguCalendarRange: EnguCalendarRange? = null
    var minYear = 1900
    var maxYear = 2100
    val onDismiss = MutableLiveData<Unit>()

    fun dismiss() {
        onDismiss.value = Unit
    }

    fun setInitSelectedDay(date: String, format: String) {
        initSelectedDay = CalendarUtils.getCalendar(format, date)
    }
}