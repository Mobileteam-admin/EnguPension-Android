package com.enugu.pension.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.enugu.pension.data.NetworkRepo
import com.enugu.pension.data.TransactionHistoryPagingSource
import com.enugu.pension.model.response.TransactionHistoryResponse
import com.enugu.pension.model.response.VerificationHistoryResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class VerificationHistoryViewModel(private val networkRepo: NetworkRepo) : ViewModel() {
    private val _verificationHistoryApiResult = MutableLiveData<VerificationHistoryResponse>()
    val verificationHistoryApiResult: LiveData<VerificationHistoryResponse>
        get() = _verificationHistoryApiResult

    fun fetchVerificationHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _verificationHistoryApiResult.postValue(networkRepo.fetchVerificationHistory())
            } catch (e: Exception) {
                _verificationHistoryApiResult.postValue(
                    VerificationHistoryResponse(
                        VerificationHistoryResponse.Detail(message = "Something went wrong with fetching verification history.")
                    )
                )
            }
        }
    }
}