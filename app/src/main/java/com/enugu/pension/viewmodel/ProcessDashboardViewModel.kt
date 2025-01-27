package com.enugu.pension.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enugu.pension.data.NetworkRepo
import com.enugu.pension.model.response.ProcessVerifyDetail
import com.enugu.pension.model.response.ResponseActiveProcessingVerify
import kotlinx.coroutines.Dispatchers
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