package com.example.engu_pension_verification_application.view_models

import android.app.Application
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.engu_pension_verification_application.Constants.AppConstants
import com.example.engu_pension_verification_application.commons.Loader
import com.example.engu_pension_verification_application.data.ApiResult
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.model.input.InputRefreshToken
import com.example.engu_pension_verification_application.model.response.AccountTypeItem
import com.example.engu_pension_verification_application.model.response.BanksDetail
import com.example.engu_pension_verification_application.model.response.ListBanksItem
import com.example.engu_pension_verification_application.model.response.ResponseBankList
import com.example.engu_pension_verification_application.model.response.ResponseRefreshToken
import com.example.engu_pension_verification_application.model.response.TokenDetail
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.ui.activity.SignUpActivity
import com.example.engu_pension_verification_application.utils.SharedPref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TokenRefreshViewModel2(private val networkRepo: NetworkRepo) : ViewModel() {

    private val _tokenRefreshError = MutableLiveData<String?>(null)
    val tokenRefreshError: LiveData<String?>
        get() = _tokenRefreshError

    suspend fun fetchRefreshToken(): Boolean {
        try {
            val response = networkRepo.fetchRefreshToken()
            if (response.token_detail?.status == AppConstants.SUCCESS) {
                SharedPref.access_token = response.token_detail.access
                SharedPref.refresh_token = response.token_detail.refresh
                return true
            } else {
                _tokenRefreshError.postValue(null)
                _tokenRefreshError.postValue(response.token_detail?.message ?: "")
            }
        } catch (e: Exception) {
            _tokenRefreshError.postValue(null)
            _tokenRefreshError.postValue("")
        }
        return false
    }
}