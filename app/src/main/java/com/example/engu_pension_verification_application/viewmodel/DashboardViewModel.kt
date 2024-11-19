package com.example.engu_pension_verification_application.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.model.response.*
import com.example.engu_pension_verification_application.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
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
}