package com.enugu.pension.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enugu.pension.data.NetworkRepo
import com.enugu.pension.model.request.InputResetPassword
import com.enugu.pension.model.response.ResetDetail
import com.enugu.pension.model.response.ResponseResetPassword
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ResetPasswordViewModel(private val networkRepo: NetworkRepo) : ViewModel() {
    private val _resetPassResponse = MutableLiveData<ResponseResetPassword>()
    val resetPassResponse: LiveData<ResponseResetPassword>
        get() = _resetPassResponse

    fun doReset(inputResetPassword: InputResetPassword) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _resetPassResponse.postValue(networkRepo.resetPassword(inputResetPassword))
            } catch (e: Exception) {
                _resetPassResponse.postValue(ResponseResetPassword(ResetDetail(message = "Something went wrong")))
            }
        }
    }
}