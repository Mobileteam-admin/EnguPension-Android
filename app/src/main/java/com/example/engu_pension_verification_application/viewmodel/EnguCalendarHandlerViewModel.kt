package com.example.engu_pension_verification_application.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.engu_pension_verification_application.model.dto.EnguCalendarRange
import com.example.engu_pension_verification_application.util.CalendarUtils
import java.util.Calendar

class EnguCalendarHandlerViewModel : ViewModel() {
    var actionId = 1
    var initSelectedDay:Calendar? = null
    val onDateSelect = MutableLiveData<Calendar?>(null)
    var enguCalendarRange:EnguCalendarRange? = null
    var minYear = 1900
    var maxYear = 2100
    val onDismiss = MutableLiveData<Unit>()

    fun dismiss() {
        onDismiss.value = Unit
    }

    fun setInitSelectedDay(date:String, format:String) {
        initSelectedDay = CalendarUtils.getCalendar(format, date)
    }
}