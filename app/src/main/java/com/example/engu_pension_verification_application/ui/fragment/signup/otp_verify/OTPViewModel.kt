package com.example.engu_pension_verification_application.ui.fragment.signup.otp_verify

import com.example.engu_pension_verification_application.model.input.InputForgotVerify
import com.example.engu_pension_verification_application.model.response.ResendDetail
import com.example.engu_pension_verification_application.model.response.ResendotpResponse
import com.example.engu_pension_verification_application.model.response.VerifyResponse
import com.example.engu_pension_verification_application.model.response.verifyDetail
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.utils.SharedPref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class OTPViewModel(var otpViewCallBack: OtpViewCallBack) {

    //(application: Application) : AndroidViewModel(application)


    private val prefs = SharedPref

   /* private val _verificationStatus = MutableLiveData<com.example.engu_pension_verification_application.model.response.VerifyResponse>()
    val verificationStatus: LiveData<com.example.engu_pension_verification_application.model.response.VerifyResponse>
        get() = _verificationStatus

    private val _resendotpStatus = MutableLiveData<com.example.engu_pension_verification_application.model.response.ResendotpResponse>()
    val resendotpStatus: LiveData<com.example.engu_pension_verification_application.model.response.ResendotpResponse>
        get() = _resendotpStatus*/


    /*init {
        application.let { prefs.with(it) }
    }*/

    fun doVerifyReg(inputSignupVerify: com.example.engu_pension_verification_application.model.input.InputSignupVerify) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = ApiClient.getApiInterface().getVerifyRegistrationOTP(inputSignupVerify)
                //_verificationStatus.value = response
                if (response.detail?.status.equals("success")) {
                    prefs.isLogin = true
                    prefs.user_id = response.detail?.userdetails?.id
                    prefs.user_name = response.detail?.userdetails?.username
                    prefs.email = response.detail?.userdetails?.email
                    //_verificationStatus.value = response
                    otpViewCallBack.onOtpVerifySuccess(response)
                } else {
                    //_verificationStatus.value = response
                    otpViewCallBack.onOtpVerifyFailure(response)
                }

            } catch (e: java.lang.Exception) {
                /*_verificationStatus.value =
                    com.example.engu_pension_verification_application.model.response.VerifyResponse(
                        com.example.engu_pension_verification_application.model.response.verifyDetail(
                            message = "Something went wrong"
                        )
                    )*/

                otpViewCallBack.onOtpVerifyFailure(
                    VerifyResponse(
                        verifyDetail(message = "Something went wrong")
                    ))
            }
        }
    }

    fun doVerifyForgot(inputForgotVerify: InputForgotVerify) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = ApiClient.getApiInterface().getVerifyForgotOTP(inputForgotVerify)
                //_verificationStatus.value = response
                if (response.detail?.status.equals("success")) {
                    prefs.isLogin = true
                    prefs.user_id = response.detail?.userdetails?.id
                    prefs.user_name = response.detail?.userdetails?.username
                    prefs.email = response.detail?.userdetails?.email
                    //_verificationStatus.value = response
                    otpViewCallBack.onOtpVerifySuccess(response)
                } else {
                    //_verificationStatus.value = response
                    otpViewCallBack.onOtpVerifyFailure(response)
                }

            } catch (e: java.lang.Exception) {
                //_verificationStatus.value =
                    com.example.engu_pension_verification_application.model.response.VerifyResponse(
                        com.example.engu_pension_verification_application.model.response.verifyDetail(
                            message = "Something went wrong"
                        )
                    )
            }
        }
    }


    fun doResendOtp(inputResendotp: com.example.engu_pension_verification_application.model.input.InputResendotp) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
               // Log.d("TAG _ 2", "onClicked: " + inputResendotp)
                val response = ApiClient.getApiInterface().getResendOTP(inputResendotp)
                //Log.d("TAG _ 2.1", "onClicked: " + response.detail?.message)

                 if (response.detail?.status.equals("success")) {
                    // Log.d("TAG _ 3", "onClicked: "+response)
                     //_resendotpStatus.value = response
                     otpViewCallBack.onOtpResendSuccess(response)
                 } else {
                     //_resendotpStatus.value = response
                     otpViewCallBack.onOtpResendFailure(response)
                 }


            } catch (e: java.lang.Exception) {
                /*_resendotpStatus.value =
                    com.example.engu_pension_verification_application.model.response.ResendotpResponse(
                        com.example.engu_pension_verification_application.model.response.ResendDetail(
                            message = "Something went wrong"
                        )
                    )*/

                otpViewCallBack.onOtpResendFailure(
                    ResendotpResponse(
                    ResendDetail(message = "Something went wrong")
                )
                )

            }
        }
    }
}