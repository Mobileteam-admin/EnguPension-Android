package com.example.engu_pension_verification_application.ui.fragment.signup.login

import com.example.engu_pension_verification_application.model.response.ResponseForgotPassword
import com.example.engu_pension_verification_application.model.response.ResponseLogin

interface LoginViewCallBack {

    fun onLoginSuccess(response : ResponseLogin)

    fun onLoginFail(response : ResponseLogin)

}