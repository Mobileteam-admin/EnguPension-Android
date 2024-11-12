package com.example.engu_pension_verification_application.ui.fragment.tokenrefresh

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.engu_pension_verification_application.model.input.InputRefreshToken
import com.example.engu_pension_verification_application.model.response.ResponseRefreshToken
import com.example.engu_pension_verification_application.model.response.TokenDetail
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.utils.SharedPref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TokenRefreshViewModel(var tokenRefreshCallBack: TokenRefreshCallBack) {
//(application: Application) : AndroidViewModel(application)
    private val prefs = SharedPref

    private val _tokenrefreshStatus = MutableLiveData<ResponseRefreshToken>()
    val TokenrefreshStatus: LiveData<ResponseRefreshToken>
        get() = _tokenrefreshStatus

    /*init {
        application.let { prefs.with(it) }
    }*/

    fun getTokenRefresh() {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response =
                    ApiClient.getRetrofit().getRefreshToken(
                        InputRefreshToken(
                            prefs.refresh_token
                        )
                    )

                if (response.token_detail?.status.equals("success")) {
                    prefs.access_token = response.token_detail?.access
                    prefs.refresh_token = response.token_detail?.refresh
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
                tokenRefreshCallBack.onTokenRefreshFailure(ResponseRefreshToken(TokenDetail(message = "Something went wrong"))
                )
            }
        }
    }
}