package com.enugu.pension.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enugu.pension.data.repository.NetworkRepo
import com.enugu.pension.data.remote.dto.request.NextOfKinRequest
import com.enugu.pension.data.remote.dto.response.NextOfKinResponse
import com.enugu.pension.data.remote.dto.response.NextOfKinUpdateResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NextOfKinProfileViewModel(private val networkRepo: NetworkRepo) : ViewModel() {
    private val _nexOfKinFetchResult = MutableLiveData<NextOfKinResponse>(null)
    val nexOfKinFetchResult: LiveData<NextOfKinResponse>
        get() = _nexOfKinFetchResult

    private val _nexOfKinSubmitResult =
        MutableLiveData<Pair<NextOfKinRequest, NextOfKinUpdateResponse>>(null)
    val nexOfKinSubmitResult: LiveData<Pair<NextOfKinRequest, NextOfKinUpdateResponse>>
        get() = _nexOfKinSubmitResult

    fun fetchProfileDetails() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _nexOfKinFetchResult.postValue(networkRepo.fetchNextOfKinDetails())
            } catch (e: Exception) {
                _nexOfKinFetchResult.postValue(
                    NextOfKinResponse(NextOfKinResponse.Detail(message = "Something went wrong with fetching next of kin profile details."))
                )
            }
        }
    }

    fun updateNextOfKinDetails(nextOfKinRequest: NextOfKinRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _nexOfKinSubmitResult.postValue(
                    Pair(nextOfKinRequest, networkRepo.submitNextOfKinDetails(nextOfKinRequest))
                )
            } catch (e: Exception) {
                _nexOfKinSubmitResult.postValue(
                    Pair(
                        nextOfKinRequest,
                        NextOfKinUpdateResponse(NextOfKinUpdateResponse.Detail(message = "Something went wrong with submitting next of kin profile details."))
                    )
                )
            }
        }
    }

}