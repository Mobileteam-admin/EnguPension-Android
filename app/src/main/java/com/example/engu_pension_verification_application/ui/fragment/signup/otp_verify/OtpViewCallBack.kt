package com.example.engu_pension_verification_application.ui.fragment.signup.otp_verify

import com.example.engu_pension_verification_application.model.response.ResendotpResponse
import com.example.engu_pension_verification_application.model.response.VerifyResponse

interface OtpViewCallBack {
    fun onOtpResendSuccess(response: ResendotpResponse)

    fun onOtpResendFailure(response: ResendotpResponse)

    fun onOtpVerifySuccess(response: VerifyResponse)

    fun onOtpVerifyFailure(response: VerifyResponse)
}