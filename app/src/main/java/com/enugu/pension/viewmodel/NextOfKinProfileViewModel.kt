package com.enugu.pension.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enugu.pension.data.NetworkRepo
import com.enugu.pension.model.request.UpdateProfileForm
import com.enugu.pension.model.response.NextOfKinResponse
import com.enugu.pension.model.response.ProfileResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class NextOfKinProfileViewModel(private val networkRepo: NetworkRepo) : ViewModel() {
    private val _nexOfKinApiResult = MutableLiveData<NextOfKinResponse>(null)
    val nexOfKinApiResult: LiveData<NextOfKinResponse>
        get() = _nexOfKinApiResult

    fun fetchProfileDetails() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _nexOfKinApiResult.postValue(networkRepo.fetchNextOfKinDetails())
            } catch (e: Exception) {
                _nexOfKinApiResult.postValue(
                    NextOfKinResponse(NextOfKinResponse.Detail(message = "Something went wrong with fetching next of kin profile details."))
                )
            }
        }
    }

}