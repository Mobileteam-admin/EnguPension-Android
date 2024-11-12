package com.example.engu_pension_verification_application.ui.fragment.signup.resetpassword

import com.example.engu_pension_verification_application.model.response.ResponseResetPassword

interface ResetPassCallBack {

    fun onResetPassSuccess(response: ResponseResetPassword)

    fun onResetPassFailure(response: ResponseResetPassword)

}