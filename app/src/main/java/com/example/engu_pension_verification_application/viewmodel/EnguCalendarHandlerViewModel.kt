package com.example.engu_pension_verification_application.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.engu_pension_verification_application.model.dto.EnguCalendarRange
import java.util.Calendar

class EnguCalendarHandlerViewModel : ViewModel() {
    var actionId = 1
    var initSelectedDay:Calendar? = null
    val onDateSelect = MutableLiveData<Calendar?>(null)
    var enguCalendarRange:EnguCalendarRange? = null
    val onDismiss = MutableLiveData<Unit>()

    fun dismiss() {
        onDismiss.value = Unit
    }
}