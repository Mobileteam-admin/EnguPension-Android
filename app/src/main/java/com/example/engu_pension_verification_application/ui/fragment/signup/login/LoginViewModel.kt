package com.example.engu_pension_verification_application.ui.fragment.signup.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.engu_pension_verification_application.model.input.InputLogin
import com.example.engu_pension_verification_application.model.response.Detail
import com.example.engu_pension_verification_application.model.response.LoginDetail
import com.example.engu_pension_verification_application.model.response.ResponseLogin
import com.example.engu_pension_verification_application.model.response.SignupResponse
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.utils.SharedPref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LoginViewModel(var loginViewCallBack: LoginViewCallBack){
    //(application: Application):AndroidViewModel(application)


    private val prefs = SharedPref

    private val _loginStatus = MutableLiveData<com.example.engu_pension_verification_application.model.response.ResponseLogin>()
    val loginStatus: LiveData<com.example.engu_pension_verification_application.model.response.ResponseLogin>
        get() = _loginStatus

    /*init {
        application.let { prefs.with(it) }
    }*/
    fun doLogin(inputLogin: com.example.engu_pension_verification_application.model.input.InputLogin) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = ApiClient.getRetrofit().getLogin(inputLogin)

                if (response.login_detail?.status.equals("success")) {
                    prefs.isLogin = true
                    prefs.user_id = response.login_detail?.user_id.toString()
                    prefs.access_token = response.login_detail?.accessToken
                    prefs.refresh_token = response.login_detail?.refreshToken
                    //_loginStatus.value = response
                    loginViewCallBack.onLoginSuccess(response)
                } else {
                    loginViewCallBack.onLoginFail(response)
                    //_loginStatus.value = response
                }

            } catch (e: java.lang.Exception) {
                _loginStatus.value =
                    com.example.engu_pension_verification_application.model.response.ResponseLogin(
                        com.example.engu_pension_verification_application.model.response.LoginDetail(
                            message = "Something went wrong"
                        )
                    )
            }
        }
    }
}