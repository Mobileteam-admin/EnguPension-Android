package com.example.engu_pension_verification_application.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.engu_pension_verification_application.Constants.AppConstants
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.util.SharedPref

class TokenRefreshViewModel2(private val networkRepo: NetworkRepo) : ViewModel() {

    private val _tokenRefreshError = MutableLiveData<String?>(null)
    val tokenRefreshError: LiveData<String?>
        get() = _tokenRefreshError

    suspend fun fetchRefreshToken(): Boolean {
        try {
            val response = networkRepo.fetchRefreshToken()
            if (response.tokenDetail?.status == AppConstants.SUCCESS) {
                SharedPref.access_token = response.tokenDetail.accessToken
                SharedPref.refresh_token = response.tokenDetail.refreshToken
                return true
            } else {
                postExpiredError()
            }
        } catch (e: Exception) {
            postExpiredError()
        }
        return false
    }
    private fun postExpiredError() {
        _tokenRefreshError.postValue(null)
        _tokenRefreshError.postValue("Session expired. Please log in again.")
    }
}