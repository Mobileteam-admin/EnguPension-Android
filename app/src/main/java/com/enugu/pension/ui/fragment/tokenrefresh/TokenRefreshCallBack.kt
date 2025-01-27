package com.enugu.pension.ui.fragment.tokenrefresh

import com.enugu.pension.model.response.ResponseRefreshToken

interface TokenRefreshCallBack {

    fun onTokenRefreshSuccess(response: ResponseRefreshToken)

    fun onTokenRefreshFailure(response: ResponseRefreshToken)

}