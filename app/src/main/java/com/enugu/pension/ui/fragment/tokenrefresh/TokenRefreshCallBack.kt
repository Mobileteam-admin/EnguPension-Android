package com.enugu.pension.ui.fragment.tokenrefresh

import com.enugu.pension.data.remote.dto.response.ResponseRefreshToken

interface TokenRefreshCallBack {

    fun onTokenRefreshSuccess(response: com.enugu.pension.data.remote.dto.response.ResponseRefreshToken)

    fun onTokenRefreshFailure(response: com.enugu.pension.data.remote.dto.response.ResponseRefreshToken)

}