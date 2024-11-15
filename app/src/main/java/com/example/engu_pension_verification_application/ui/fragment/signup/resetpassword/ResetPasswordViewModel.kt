package com.example.engu_pension_verification_application.ui.fragment.signup.resetpassword

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.utils.SharedPref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ResetPasswordViewModel(var resetPassCallBack: ResetPassCallBack) {
    //(application: Application) : AndroidViewModel(application)
    private val prefs = SharedPref
    private val _ResetPassStatus = MutableLiveData<com.example.engu_pension_verification_application.model.response.ResponseResetPassword>()
    val resetPassStatus: LiveData<com.example.engu_pension_verification_application.model.response.ResponseResetPassword>
        get() = _ResetPassStatus
    /*init {
        application.let { prefs.with(it) }
    }*/

    fun doReset(inputResetPassword: com.example.engu_pension_verification_application.model.input.InputResetPassword) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = ApiClient.getApiInterface().getResetPassword(inputResetPassword)

                if (response.reset_detail?.status.equals("success")) {
                    //_ResetPassStatus.value = response
                    resetPassCallBack.onResetPassSuccess(response)
                } else {
                    //_ResetPassStatus.value = response
                    resetPassCallBack.onResetPassFailure(response)
                }
            }catch (e: java.lang.Exception) {
                _ResetPassStatus.value =
                    com.example.engu_pension_verification_application.model.response.ResponseResetPassword(
                        com.example.engu_pension_verification_application.model.response.ResetDetail(
                            message = "Something went wrong"
                        )
                    )
            }
        }
    }
}