package com.example.engu_pension_verification_application.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.engu_pension_verification_application.Constants.AppConstants
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.model.input.BookAppointmentRequest
import com.example.engu_pension_verification_application.model.input.TransferRequest
import com.example.engu_pension_verification_application.model.response.BookAppointmentResponse
import com.example.engu_pension_verification_application.model.response.BookingDateRangeResponse
import com.example.engu_pension_verification_application.model.response.BookingSlotResponse
import com.example.engu_pension_verification_application.model.response.TransferResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BookingDetailsViewModel(private val networkRepo: NetworkRepo) : ViewModel() {

    private val _transferApiResult = MutableLiveData<Pair<String,TransferResponse>>()
    val transferApiResult: LiveData<Pair<String,TransferResponse>>
        get() = _transferApiResult

    fun transferToFinalAccount(description: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val transferRequest = TransferRequest(1400f,"USD",description)
            try {
                _transferApiResult.postValue(
                    Pair(description, networkRepo.transferToFinalAccount(transferRequest))
                )
            } catch (e: Exception) {
                _transferApiResult.postValue(
                    Pair(
                        description,
                        TransferResponse(TransferResponse.Detail(message = "Something went wrong"))
                    )
                )
            }
        }
    }
}