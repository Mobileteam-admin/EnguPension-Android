package com.enugu.pension.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enugu.pension.data.remote.ApiResult
import com.enugu.pension.data.repository.NetworkRepo
import com.enugu.pension.data.remote.dto.request.InputLGAList
import com.enugu.pension.data.remote.dto.request.InputRetireeBasicDetails
import com.enugu.pension.data.remote.dto.response.*
import com.enugu.pension.common.util.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RetireeBasicDetailsViewModel(private val networkRepo: NetworkRepo) :ViewModel() {
    private val _combinedDetailsApiResult = MutableLiveData<ResponseCombinationDetails>()
    val combinedDetailsApiResult: LiveData<ResponseCombinationDetails>
        get() = _combinedDetailsApiResult

    private val _basicDetailsApiResult = MutableLiveData<ResponseRetireeBasicRetrive>()
    val basicDetailsApiResult: LiveData<ResponseRetireeBasicRetrive>
        get() = _basicDetailsApiResult

    private val _basicDetailsSubmissionResult =
        MutableLiveData<Pair<InputRetireeBasicDetails, com.enugu.pension.data.remote.dto.response.ResponseRetireeBasicDetails>?>(null)
    val basicDetailsSubmissionResult: LiveData<Pair<InputRetireeBasicDetails, com.enugu.pension.data.remote.dto.response.ResponseRetireeBasicDetails>?>
        get() = _basicDetailsSubmissionResult

    suspend fun fetchCombinedDetails(country: String): Boolean {
        try {
            _combinedDetailsApiResult.postValue(networkRepo.fetchCombinedDetails(InputLGAList(country)))
            return true
        } catch (e: Exception) {
            _combinedDetailsApiResult.postValue(
                ResponseCombinationDetails(
                    CombinationDetail(message = "Something went wrong with fetching combined details")
                )
            )
        }
        return false
    }

    fun fetchRetireeBasicDetails() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _basicDetailsApiResult.postValue(networkRepo.fetchRetireeBasicDetails())
            } catch (e: Exception) {
                _basicDetailsApiResult.postValue(
                    ResponseRetireeBasicRetrive(
                        RetireeRetriveDetail(message = "Something went wrong with fetching basic details")
                    )
                )
            }
        }
    }

    fun submitBasicDetails(inputRetireeBasicDetails: InputRetireeBasicDetails) {
        viewModelScope.launch(Dispatchers.IO) {
            val unknownErrorMsg = "Something went wrong with basic details submission"
            val call = networkRepo.submitRetireeBasicDetails(inputRetireeBasicDetails)
            val response = when (val apiResult = NetworkUtils.handleResponse(call,unknownErrorMsg)) {
                is ApiResult.Success -> apiResult.data
                is ApiResult.Error ->
                    com.enugu.pension.data.remote.dto.response.ResponseRetireeBasicDetails(
                        com.enugu.pension.data.remote.dto.response.RetireeBasicDetail(message = apiResult.message)
                    )
            }
            _basicDetailsSubmissionResult.postValue(
                Pair(
                    inputRetireeBasicDetails,
                    response
                )
            )
        }
    }

    fun resetBasicDetailsSubmissionResult() {
        _basicDetailsSubmissionResult.value = null
    }
}