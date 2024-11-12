package com.example.engu_pension_verification_application.ui.fragment.signup.sign_up

import com.example.engu_pension_verification_application.model.response.SignupResponse
import com.example.engu_pension_verification_application.model.response.VerifyResponse

interface SignUpCallBack {

    fun onSignUpSuccess(response: SignupResponse)

    fun onSignUpFailure(response: SignupResponse)
}