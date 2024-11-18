package com.example.engu_pension_verification_application.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.model.input.InputLogin
import com.example.engu_pension_verification_application.model.response.LoginDetail
import com.example.engu_pension_verification_application.model.response.ResponseLogin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginViewModel(private val networkRepo: NetworkRepo) : ViewModel() {
    private val _loginStatus = MutableLiveData<ResponseLogin>()
    val loginStatus: LiveData<ResponseLogin>
        get() = _loginStatus

    fun doLogin(inputLogin: InputLogin) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _loginStatus.postValue(networkRepo.login(inputLogin))
            } catch (e: Exception) {
                _loginStatus.postValue(ResponseLogin(LoginDetail(message = "Something went wrong")))
            }
        }
    }
}