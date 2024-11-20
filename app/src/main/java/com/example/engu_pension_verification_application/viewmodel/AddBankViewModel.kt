package com.example.engu_pension_verification_application.viewmodel

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

class AddBankViewModel(private val networkRepo: NetworkRepo) : ViewModel() {

}