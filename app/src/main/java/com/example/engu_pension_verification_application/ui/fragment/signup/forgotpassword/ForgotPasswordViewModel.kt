package com.example.engu_pension_verification_application.ui.fragment.signup.forgotpassword

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.model.input.InputForgotPassword
import com.example.engu_pension_verification_application.model.response.ForgotPasswordDetail
import com.example.engu_pension_verification_application.model.response.ResponseForgotPassword
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
class ForgotPasswordViewModel(private val networkRepo: NetworkRepo) : ViewModel() {
    private val _forgotPassResponse = MutableLiveData<ResponseForgotPassword>()
    val forgotPassResponse: LiveData<ResponseForgotPassword>
        get() = _forgotPassResponse

    fun doForgotPass(inputForgotPassword: InputForgotPassword) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _forgotPassResponse.postValue(networkRepo.forgotPassword(inputForgotPassword))
            } catch (e: Exception) {
                _forgotPassResponse.postValue(ResponseForgotPassword(ForgotPasswordDetail(message = "Something went wrong")))
            }
        }
    }
}