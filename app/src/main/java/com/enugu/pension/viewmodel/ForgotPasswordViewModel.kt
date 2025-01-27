package com.enugu.pension.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enugu.pension.data.NetworkRepo
import com.enugu.pension.model.request.InputForgotPassword
import com.enugu.pension.model.response.ForgotPasswordDetail
import com.enugu.pension.model.response.ResponseForgotPassword
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
class ForgotPasswordViewModel(private val networkRepo: NetworkRepo) : ViewModel() {
    private val _forgotPassResponse = MutableLiveData<ResponseForgotPassword>()
    val forgotPassResponse: LiveData<ResponseForgotPassword>
        get() = _forgotPassResponse

    fun doForgotPass(inputForgotPassword: InputForgotPassword) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _forgotPassResponse.postValue(networkRepo.forgotPassword(inputForgotPassword))
            } catch (e: Exception) {
                _forgotPassResponse.postValue(ResponseForgotPassword(ForgotPasswordDetail(message = "Something went wrong")))
            }
        }
    }
}