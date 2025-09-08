package com.enugu.pension.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enugu.pension.data.remote.ApiResult
import com.enugu.pension.data.repository.NetworkRepo
import com.enugu.pension.data.remote.dto.request.TransferRequest
import com.enugu.pension.data.remote.dto.response.TransferResponse
import com.enugu.pension.common.util.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BookingDetailsViewModel(private val networkRepo: NetworkRepo) : ViewModel() {

    private val _transferApiResult = MutableLiveData<Pair<String, TransferResponse>>()
    val transferApiResult: LiveData<Pair<String, TransferResponse>>
        get() = _transferApiResult

    fun transferToFinalAccount(description: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val request = TransferRequest(1400f, "USD", description)
            val call = networkRepo.transferToFinalAccount(request)
            val response = when (val apiResult = NetworkUtils.handleResponse(call)) {
                is ApiResult.Success -> apiResult.data
                is ApiResult.Error -> TransferResponse(TransferResponse.Detail(message = apiResult.message))
            }
            networkRepo.createVideoCallRoom()
            _transferApiResult.postValue(Pair(description, response))
        }
    }
}