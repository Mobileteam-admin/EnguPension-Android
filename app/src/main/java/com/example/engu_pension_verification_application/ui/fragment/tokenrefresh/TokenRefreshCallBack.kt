package com.example.engu_pension_verification_application.ui.fragment.tokenrefresh

import com.example.engu_pension_verification_application.model.response.ResponseActiveBasicDetails
import com.example.engu_pension_verification_application.model.response.ResponseRefreshToken

interface TokenRefreshCallBack {

    fun onTokenRefreshSuccess(response: ResponseRefreshToken)

    fun onTokenRefreshFailure(response: ResponseRefreshToken)

}