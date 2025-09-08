package com.enugu.pension.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.enugu.pension.data.remote.dto.request.InputRefreshToken
import com.enugu.pension.data.remote.api.ApiClient
import com.enugu.pension.ui.fragment.tokenrefresh.TokenRefreshCallBack
import com.enugu.pension.data.local.SharedPref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TokenRefreshViewModel(var tokenRefreshCallBack: TokenRefreshCallBack) {
//(application: Application) : AndroidViewModel(application)
    private val prefs = SharedPref

    private val _tokenrefreshStatus = MutableLiveData<com.enugu.pension.data.remote.dto.response.ResponseRefreshToken>()
    val TokenrefreshStatus: LiveData<com.enugu.pension.data.remote.dto.response.ResponseRefreshToken>
        get() = _tokenrefreshStatus

    /*init {
        application.let { prefs.with(it) }
    }*/

    fun getTokenRefresh() {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response =
                    ApiClient.getApiInterface().getRefreshToken(
                        InputRefreshToken(
                            prefs.refresh_token
                        )
                    )

                if (response.tokenDetail?.status.equals("success")) {
                    prefs.access_token = response.tokenDetail?.accessToken
                    prefs.refresh_token = response.tokenDetail?.refreshToken
                    //_tokenrefreshStatus.value = response
                    tokenRefreshCallBack.onTokenRefreshSuccess(response)
                } else {
                    //_tokenrefreshStatus.value = response
                    tokenRefreshCallBack.onTokenRefreshFailure(response)
                }

            } catch (e: java.lang.Exception) {
                /*_tokenrefreshStatus.value =
                    ResponseRefreshToken(
                        TokenDetail(
                            message = "Something went wrong"
                        )
                    )*/
                tokenRefreshCallBack.onTokenRefreshFailure(
                    com.enugu.pension.data.remote.dto.response.ResponseRefreshToken(
                        com.enugu.pension.data.remote.dto.response.TokenDetail(
                            message = "Something went wrong"
                        )
                    )
                )
            }
        }
    }
}