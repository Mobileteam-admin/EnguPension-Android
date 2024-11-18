package com.example.engu_pension_verification_application.ui.fragment.signup.resetpassword

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.model.input.InputResetPassword
import com.example.engu_pension_verification_application.model.response.ResetDetail
import com.example.engu_pension_verification_application.model.response.ResponseResetPassword
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ResetPasswordViewModel(private val networkRepo: NetworkRepo) : ViewModel() {
    private val _resetPassResponse = MutableLiveData<ResponseResetPassword>()
    val resetPassResponse: LiveData<ResponseResetPassword>
        get() = _resetPassResponse

    fun doReset(inputResetPassword: InputResetPassword) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _resetPassResponse.postValue(networkRepo.resetPassword(inputResetPassword))
            } catch (e: Exception) {
                _resetPassResponse.postValue(ResponseResetPassword(ResetDetail(message = "Something went wrong")))
            }
        }
    }
}