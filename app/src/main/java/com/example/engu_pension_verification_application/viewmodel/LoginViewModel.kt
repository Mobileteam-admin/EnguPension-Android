package com.example.engu_pension_verification_application.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.engu_pension_verification_application.Constants.AppConstants
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.model.input.InputLogin
import com.example.engu_pension_verification_application.model.response.LoginDetail
import com.example.engu_pension_verification_application.model.response.ResponseLogin
import com.example.engu_pension_verification_application.util.OnboardingStage
import com.example.engu_pension_verification_application.util.SharedPref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginViewModel(private val networkRepo: NetworkRepo) : ViewModel() {
    private val _loginStatus = MutableLiveData<ResponseLogin>()
    val loginStatus: LiveData<ResponseLogin>
        get() = _loginStatus

    fun doLogin(inputLogin: InputLogin) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val loginResponse = networkRepo.login(inputLogin)
                if (loginResponse.login_detail?.status == AppConstants.SUCCESS) {
                    SharedPref.isLogin = true
                    SharedPref.user_id = loginResponse.login_detail.user_id.toString()
                    SharedPref.access_token = loginResponse.login_detail.accessToken
                    SharedPref.refresh_token = loginResponse.login_detail.refreshToken
                    networkRepo.getAccountCompletionStatus().detail?.let {
                        if (it.status == AppConstants.SUCCESS && it.userAccountDetailsSaveCompleted)
                            SharedPref.onboardingStage = OnboardingStage.DASHBOARD
                    }
                }
                _loginStatus.postValue(loginResponse)
            } catch (e: Exception) {
                _loginStatus.postValue(ResponseLogin(LoginDetail(message = "Something went wrong")))
            }
        }
    }
}