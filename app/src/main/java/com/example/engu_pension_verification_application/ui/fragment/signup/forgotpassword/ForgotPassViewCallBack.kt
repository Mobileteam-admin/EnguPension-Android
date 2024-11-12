package com.example.engu_pension_verification_application.ui.fragment.signup.forgotpassword

import com.example.engu_pension_verification_application.model.response.ResponseForgotPassword

interface ForgotPassViewCallBack {

    fun onForgotPassSuccess(response: ResponseForgotPassword)

    fun onForgotPassFail(response: ResponseForgotPassword)

}