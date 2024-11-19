package com.example.engu_pension_verification_application.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.model.input.InputSignup
import com.example.engu_pension_verification_application.model.response.Detail
import com.example.engu_pension_verification_application.model.response.SignupResponse
import kotlinx.coroutines.*

class SignUpViewModel(private val networkRepo: NetworkRepo) : ViewModel() {
    private val _signupStatus = MutableLiveData<SignupResponse>()
    val signupStatus: LiveData<SignupResponse>
        get() = _signupStatus

    fun doSignup(inputSignup: InputSignup) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _signupStatus.postValue(networkRepo.signUp(inputSignup))
            } catch (e: java.lang.Exception) {
                _signupStatus.postValue(SignupResponse(Detail(message = "Something went wrong")))
            }
        }
    }
}