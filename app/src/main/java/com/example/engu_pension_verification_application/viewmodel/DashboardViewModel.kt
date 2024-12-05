package com.example.engu_pension_verification_application.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.engu_pension_verification_application.data.ApiResult
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.model.input.VideoCallRequest
import com.example.engu_pension_verification_application.model.response.*
import com.example.engu_pension_verification_application.util.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DashboardViewModel(private val networkRepo: NetworkRepo) : ViewModel() {
    private val _logoutResult =
        MutableLiveData<ResponseLogout>()
    val logoutResult: LiveData<ResponseLogout>
        get() = _logoutResult

    private val _dashboardDetailsResult =
        MutableLiveData<ResponseDashboardDetails>()
    val dashboardDetailsResult: LiveData<ResponseDashboardDetails>
        get() = _dashboardDetailsResult

    private val _videoCallApiResult = MutableLiveData<Pair<VideoCallRequest,VideoCallResponse>>()
    val videoCallApiResult: LiveData<Pair<VideoCallRequest,VideoCallResponse>>
        get() = _videoCallApiResult

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _logoutResult.postValue(networkRepo.logout())
            } catch (e: Exception) {
                _logoutResult.postValue(
                    ResponseLogout(
                        LogoutDetail(message = "Something went wrong")
                    )
                )
            }
        }
    }

    fun fetchDashboardDetails() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _dashboardDetailsResult.postValue(networkRepo.fetchDashboardDetails())
            } catch (e: Exception) {
                _dashboardDetailsResult.postValue(
                    ResponseDashboardDetails(
                        DashboardDetails(message = "Something went wrong with fetching dashboard details")
                    )
                )
            }
        }
    }

    fun fetchVideoCallLink(request: VideoCallRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            val call = networkRepo.fetchVideoCallLink(request)
            val response = when (val apiResult = NetworkUtils.handleResponse(call)) {
                is ApiResult.Success -> apiResult.data
                is ApiResult.Error ->
                    VideoCallResponse(
                        VideoCallResponse.Detail(message = apiResult.message)
                    )
            }
            _videoCallApiResult.postValue(
                Pair(
                    request,
                    response
                )
            )
        }
    }
}