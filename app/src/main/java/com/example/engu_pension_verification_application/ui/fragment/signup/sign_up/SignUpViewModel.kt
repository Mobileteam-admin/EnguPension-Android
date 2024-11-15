package com.example.engu_pension_verification_application.ui.fragment.signup.sign_up

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.utils.SharedPref
import kotlinx.coroutines.*

class SignUpViewModel(var signUpCallBack: SignUpCallBack) {
    //(application: Application) : AndroidViewModel(application)

    private val prefs = SharedPref

    private val _signupStatus = MutableLiveData<com.example.engu_pension_verification_application.model.response.SignupResponse>()
    val signupStatus: LiveData<com.example.engu_pension_verification_application.model.response.SignupResponse>
        get() = _signupStatus


    /*init {
        application.let { prefs.with(it) }
    }*/

    fun doSignup(inputSignup: com.example.engu_pension_verification_application.model.input.InputSignup) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = ApiClient.getApiInterface().getSignUp(inputSignup)

                if (response.detail?.status.equals("success")) {
                    //_signupStatus.value = response
                    signUpCallBack.onSignUpSuccess(response)
                } else {
                    //_signupStatus.value = response
                    signUpCallBack.onSignUpFailure(response)
                }

            } catch (e: java.lang.Exception) {
                _signupStatus.value =
                    com.example.engu_pension_verification_application.model.response.SignupResponse(
                        com.example.engu_pension_verification_application.model.response.Detail(
                            message = "Something went wrong"
                        )
                    )
            }
        }
    }


}