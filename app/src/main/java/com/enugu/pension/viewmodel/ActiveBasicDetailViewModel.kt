package com.enugu.pension.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enugu.pension.data.remote.ApiResult
import com.enugu.pension.data.repository.NetworkRepo
import com.enugu.pension.data.remote.dto.request.InputActiveBasicDetails
import com.enugu.pension.data.remote.dto.request.InputLGAList
import com.enugu.pension.data.remote.dto.response.*
import com.enugu.pension.common.util.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ActiveBasicDetailViewModel(
    private val networkRepo: NetworkRepo
):ViewModel() {
    private val _combinedDetailsApiResult = MutableLiveData<ResponseCombinationDetails>()
    val combinedDetailsApiResult: LiveData<ResponseCombinationDetails>
        get() = _combinedDetailsApiResult

    private val _basicDetailsApiResult = MutableLiveData<ResponseActiveBasicRetrive>()
    val basicDetailsApiResult: LiveData<ResponseActiveBasicRetrive>
        get() = _basicDetailsApiResult

    private val _basicDetailsSubmissionResult =
        MutableLiveData<Pair<InputActiveBasicDetails, ResponseActiveBasicDetails>?>(null)
    val basicDetailsSubmissionResult: LiveData<Pair<InputActiveBasicDetails, ResponseActiveBasicDetails>?>
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

    fun fetchActiveBasicDetails() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _basicDetailsApiResult.postValue(networkRepo.fetchActiveBasicDetails())
            } catch (e: Exception) {
                e.printStackTrace()
                _basicDetailsApiResult.postValue(
                    ResponseActiveBasicRetrive(
                        ActiveRetriveDetail(message = "Something went wrong with fetching basic details")
                    )
                )
            }
        }
    }

    fun resetBasicDetailsSubmissionResult() {
        _basicDetailsSubmissionResult.value = null
    }

    fun submitActiveBasicDetails(inputActiveBasicDetails: InputActiveBasicDetails) {
        viewModelScope.launch(Dispatchers.IO) {
            val unknownErrorMsg = "Something went wrong with basic details submission"
            val call = networkRepo.submitActiveBasicDetails(inputActiveBasicDetails)
            val response = when (val apiResult = NetworkUtils.handleResponse(call,unknownErrorMsg)) {
                is ApiResult.Success -> apiResult.data
                is ApiResult.Error ->
                    ResponseActiveBasicDetails(
                        ActiveBasicDetail(message = apiResult.message)
                    )
            }
            _basicDetailsSubmissionResult.postValue(
                Pair(
                    inputActiveBasicDetails,
                    response
                )
            )
        }
    }

}