package com.enugu.pension.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enugu.pension.data.NetworkRepo
import com.enugu.pension.model.request.InputSignup
import com.enugu.pension.model.response.Detail
import com.enugu.pension.model.response.SignupResponse
import kotlinx.coroutines.*

class SignUpViewModel(private val networkRepo: NetworkRepo) : ViewModel() {
    private val _signupStatus = MutableLiveData<SignupResponse?>(null)
    val signupStatus: LiveData<SignupResponse?>
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
    fun resetSignupStatus() {
        _signupStatus.postValue(null)
    }
}