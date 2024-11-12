package com.example.engu_pension_verification_application.ui.fragment.signup.forgotpassword

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.engu_pension_verification_application.model.input.InputForgotPassword
import com.example.engu_pension_verification_application.model.response.ForgotPasswordDetail
import com.example.engu_pension_verification_application.model.response.LoginDetail
import com.example.engu_pension_verification_application.model.response.ResponseForgotPassword
import com.example.engu_pension_verification_application.model.response.ResponseLogin
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.utils.SharedPref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(var forgotPassViewCallBack: ForgotPassViewCallBack) {

    private val prefs = SharedPref

    private val _ForgotPassStatus = MutableLiveData<com.example.engu_pension_verification_application.model.response.ResponseForgotPassword>()
    val forgotPassStatus: LiveData<com.example.engu_pension_verification_application.model.response.ResponseForgotPassword>
        get() = _ForgotPassStatus

    /*init {
        application.let { prefs.with(it) }
    }*/

    fun doForgotPass(inputForgotPassword: com.example.engu_pension_verification_application.model.input.InputForgotPassword) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = ApiClient.getRetrofit().getForgotPassword(inputForgotPassword)

                if (response.forgot_detail?.status.equals("success")) {
                    //_ForgotPassStatus.value = response
                    forgotPassViewCallBack.onForgotPassSuccess(response)

                } else {
                    //_ForgotPassStatus.value = response
                    forgotPassViewCallBack.onForgotPassFail(response)
                }

            } catch (e: java.lang.Exception) {
                _ForgotPassStatus.value =
                    com.example.engu_pension_verification_application.model.response.ResponseForgotPassword(
                        com.example.engu_pension_verification_application.model.response.ForgotPasswordDetail(
                            message = "Something went wrong"
                        )
                    )
            }
        }
    }
}