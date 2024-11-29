package com.example.engu_pension_verification_application.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.engu_pension_verification_application.model.response.BookingDateRangeResponse.Detail.BookingDateRange
import java.util.Calendar

class CalendarResultViewModel : ViewModel() {
    val onDateSelect = MutableLiveData<Calendar?>(null)
    var dateRange = arrayListOf<BookingDateRange>()
}