package com.example.engu_pension_verification_application.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.model.input.InputForgotVerify
import com.example.engu_pension_verification_application.model.input.InputResendotp
import com.example.engu_pension_verification_application.model.input.InputSignupVerify
import com.example.engu_pension_verification_application.model.response.ResendDetail
import com.example.engu_pension_verification_application.model.response.ResendotpResponse
import com.example.engu_pension_verification_application.model.response.VerifyResponse
import com.example.engu_pension_verification_application.model.response.verifyDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OTPViewModel(private val networkRepo: NetworkRepo) : ViewModel() {

    private val _otpVerifyResponse = MutableLiveData<VerifyResponse>()
    val otpVerifyResponse: LiveData<VerifyResponse>
        get() = _otpVerifyResponse

    private val _verifyForgotPassResponse = MutableLiveData<VerifyResponse>()
    val verifyForgotPassResponse: LiveData<VerifyResponse>
        get() = _verifyForgotPassResponse

    private val _resendOTPResponse = MutableLiveData<ResendotpResponse>()
    val resendOTPResponse: LiveData<ResendotpResponse>
        get() = _resendOTPResponse

    fun doVerifyReg(inputSignupVerify: InputSignupVerify) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _otpVerifyResponse.postValue(networkRepo.verifyOTP(inputSignupVerify))
            } catch (e: Exception) {
                _otpVerifyResponse.postValue(VerifyResponse(verifyDetail(message = "Something went wrong")))
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
                _verifyForgotPassResponse.postValue(VerifyResponse(verifyDetail(message = "Something went wrong")))
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