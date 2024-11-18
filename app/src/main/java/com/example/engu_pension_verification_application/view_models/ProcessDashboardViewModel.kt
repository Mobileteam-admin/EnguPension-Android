package com.example.engu_pension_verification_application.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.model.input.InputLogin
import com.example.engu_pension_verification_application.model.response.LoginDetail
import com.example.engu_pension_verification_application.model.response.ProcessVerifyDetail
import com.example.engu_pension_verification_application.model.response.ResponseActiveProcessingVerify
import com.example.engu_pension_verification_application.model.response.ResponseLogin
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.utils.SharedPref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ProcessDashboardViewModel(private val networkRepo: NetworkRepo) : ViewModel() {
    private val _verificationStatus = MutableLiveData<ResponseActiveProcessingVerify>()
    val verificationStatus: LiveData<ResponseActiveProcessingVerify>
        get() = _verificationStatus

    fun getGovtVerificationStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _verificationStatus.postValue(networkRepo.getGovtVerificationStatus())
            } catch (e: Exception) {
                _verificationStatus.postValue(
                    ResponseActiveProcessingVerify(
                        ProcessVerifyDetail(message = "Something went wrong")
                    )
                )
            }
        }
    }
}