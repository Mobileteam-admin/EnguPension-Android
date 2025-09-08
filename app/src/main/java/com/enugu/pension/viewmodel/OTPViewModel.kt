package com.enugu.pension.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enugu.pension.data.repository.NetworkRepo
import com.enugu.pension.data.remote.dto.request.InputForgotVerify
import com.enugu.pension.data.remote.dto.request.InputResendotp
import com.enugu.pension.data.remote.dto.request.InputSignupVerify
import com.enugu.pension.data.remote.dto.response.ResendDetail
import com.enugu.pension.data.remote.dto.response.ResendotpResponse
import com.enugu.pension.data.remote.dto.response.VerifyResponse
import com.enugu.pension.data.remote.dto.response.verifyDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OTPViewModel(private val networkRepo: NetworkRepo) : ViewModel() {

    private val _otpVerifyResponse = MutableLiveData<com.enugu.pension.data.remote.dto.response.VerifyResponse>()
    val otpVerifyResponse: LiveData<com.enugu.pension.data.remote.dto.response.VerifyResponse>
        get() = _otpVerifyResponse

    private val _verifyForgotPassResponse = MutableLiveData<com.enugu.pension.data.remote.dto.response.VerifyResponse>()
    val verifyForgotPassResponse: LiveData<com.enugu.pension.data.remote.dto.response.VerifyResponse>
        get() = _verifyForgotPassResponse

    private val _resendOTPResponse = MutableLiveData<ResendotpResponse>()
    val resendOTPResponse: LiveData<ResendotpResponse>
        get() = _resendOTPResponse

    fun doVerifyReg(inputSignupVerify: InputSignupVerify) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _otpVerifyResponse.postValue(networkRepo.verifyOTP(inputSignupVerify))
            } catch (e: Exception) {
                _otpVerifyResponse.postValue(
                    com.enugu.pension.data.remote.dto.response.VerifyResponse(
                        com.enugu.pension.data.remote.dto.response.verifyDetail(message = "Something went wrong")
                    )
                )
            }
        }
    }

    fun doVerifyForgot(inputForgotVerify: InputForgotVerify) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _verifyForgotPassResponse.postValue(
                    networkRepo.verifyForgotPassword(
                        inputForgotVerify
                    )
                )
            } catch (e: Exception) {
                _verifyForgotPassResponse.postValue(
                    com.enugu.pension.data.remote.dto.response.VerifyResponse(
                        com.enugu.pension.data.remote.dto.response.verifyDetail(message = "Something went wrong")
                    )
                )
            }
        }
    }

    fun doResendOtp(InputResendotp: InputResendotp) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _resendOTPResponse.postValue(networkRepo.resendOTP(InputResendotp))
            } catch (e: Exception) {
                _resendOTPResponse.postValue(ResendotpResponse(ResendDetail(message = "Something went wrong")))
            }
        }
    }
}