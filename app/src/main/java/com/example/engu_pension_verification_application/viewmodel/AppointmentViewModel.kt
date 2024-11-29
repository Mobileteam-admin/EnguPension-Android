package com.example.engu_pension_verification_application.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.engu_pension_verification_application.Constants.AppConstants
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.model.input.BookAppointmentRequest
import com.example.engu_pension_verification_application.model.input.TopUpRequest
import com.example.engu_pension_verification_application.model.response.BookAppointmentResponse
import com.example.engu_pension_verification_application.model.response.BookingDateRangeResponse
import com.example.engu_pension_verification_application.model.response.BookingSlotResponse
import com.example.engu_pension_verification_application.model.response.TopUpResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AppointmentViewModel(private val networkRepo: NetworkRepo) : ViewModel() {

    var selectedDate: String? = null
    var selectedTimeSlotId: Int? = null
    private val _slotApiResult = MutableLiveData<Pair<String, BookingSlotResponse>>()
    val slotApiResult: LiveData<Pair<String, BookingSlotResponse>>
        get() = _slotApiResult


    private val _dateRangeApiResult = MutableLiveData<BookingDateRangeResponse>()
    val dateRangeApiResult: LiveData<BookingDateRangeResponse>
        get() = _dateRangeApiResult

    private val _bookAppointmentApiResult =
        MutableLiveData<Pair<BookAppointmentRequest, BookAppointmentResponse>>()
    val bookAppointmentApiResult: LiveData<Pair<BookAppointmentRequest, BookAppointmentResponse>>
        get() = _bookAppointmentApiResult

    fun fetchBookingSlots(selectedDay: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _slotApiResult.postValue(
                    Pair(selectedDay, networkRepo.fetchBookingSlots(selectedDay))
                )
            } catch (e: Exception) {
                _slotApiResult.postValue(
                    Pair(
                        selectedDay,
                        BookingSlotResponse(
                            BookingSlotResponse.Detail(message = "Something went wrong with fetching slots")
                        )
                    )
                )
            }
        }
    }

    fun fetchBookingDateRange() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _dateRangeApiResult.postValue(networkRepo.fetchBookingDateRange())
            } catch (e: Exception) {
                _dateRangeApiResult.postValue(
                    BookingDateRangeResponse(
                        BookingDateRangeResponse.Detail(message = "Something went wrong with fetching date range")
                    )
                )
            }
        }
    }

    fun bookAppointment(request: BookAppointmentRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _bookAppointmentApiResult.postValue(Pair(request, networkRepo.bookAppointment(request)))
            } catch (e: Exception) {
                _bookAppointmentApiResult.postValue(
                    Pair(
                        request,
                        BookAppointmentResponse(
                            BookAppointmentResponse.Detail(message = "Something went wrong with booking appointment")
                        )
                    )
                )
            }
        }
    }
    fun bookAppointmentCall(request: BookAppointmentRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            val call = networkRepo.bookAppointmentCall(request)
            call.enqueue(object : Callback<BookAppointmentResponse> {
                override fun onResponse(
                    call: Call<BookAppointmentResponse>,
                    response: Response<BookAppointmentResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        _bookAppointmentApiResult.postValue(Pair(request, response.body()!!))
                    } else {
                        var message: String? = null
                        try {
                            response.errorBody()?.let {
                                val jsonObject = JSONObject(it.string())
                                message = jsonObject.getJSONObject("detail").getString("message")
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            _bookAppointmentApiResult.postValue(
                                Pair(
                                    request,
                                    BookAppointmentResponse(
                                        BookAppointmentResponse.Detail(message = message?:"Something went wrong with booking appointment")
                                    )
                                )
                            )
                        }
                    }
                }

                override fun onFailure(call: Call<BookAppointmentResponse>, t: Throwable) {
                }
            })
        }
    }

}