package com.enugu.pension.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enugu.pension.data.ApiResult
import com.enugu.pension.data.NetworkRepo
import com.enugu.pension.model.request.VideoCallRequest
import com.enugu.pension.model.response.*
import com.enugu.pension.util.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Calendar

class ReservationViewModel(private val networkRepo: NetworkRepo) : ViewModel() {
    var isCallButtonEnabled = false
    var startTime: Calendar? = null
    private val _videoCallApiResult = MutableLiveData<VideoCallResponse>()
    val videoCallApiResult: LiveData<VideoCallResponse>
        get() = _videoCallApiResult

    private val _reservationApiResult = MutableLiveData<ReservationResponse>()
    val reservationApiResult: LiveData<ReservationResponse>
        get() = _reservationApiResult


    fun fetchVideoCallLink() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _videoCallApiResult.postValue(networkRepo.fetchVideoCallLink())
            } catch (e: Exception) {
                e.printStackTrace()
                _videoCallApiResult.postValue(
                    VideoCallResponse(
                        VideoCallResponse.Detail(message = "Something went wrong with video call")
                    )
                )
            }
        }
    }

    fun fetchReservationDetails() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _reservationApiResult.postValue(networkRepo.fetchReservationDetails())
            } catch (e: Exception) {
                e.printStackTrace()
                _reservationApiResult.postValue(
                    ReservationResponse(
                        ReservationResponse.Detail(message = "Something went wrong with fetching reservation details.")
                    )
                )
            }
        }
    }

}