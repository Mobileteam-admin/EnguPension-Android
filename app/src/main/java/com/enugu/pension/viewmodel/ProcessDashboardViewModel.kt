package com.enugu.pension.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enugu.pension.data.repository.NetworkRepo
import com.enugu.pension.data.remote.dto.response.ProcessVerifyDetail
import com.enugu.pension.data.remote.dto.response.ResponseActiveProcessingVerify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProcessDashboardViewModel(private val networkRepo: NetworkRepo) : ViewModel() {
    private val _verificationStatus = MutableLiveData<com.enugu.pension.data.remote.dto.response.ResponseActiveProcessingVerify>()
    val verificationStatus: LiveData<com.enugu.pension.data.remote.dto.response.ResponseActiveProcessingVerify>
        get() = _verificationStatus

    fun getGovtVerificationStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _verificationStatus.postValue(networkRepo.getGovtVerificationStatus())
            } catch (e: Exception) {
                _verificationStatus.postValue(
                    com.enugu.pension.data.remote.dto.response.ResponseActiveProcessingVerify(
                        com.enugu.pension.data.remote.dto.response.ProcessVerifyDetail(message = "Something went wrong")
                    )
                )
            }
        }
    }
}