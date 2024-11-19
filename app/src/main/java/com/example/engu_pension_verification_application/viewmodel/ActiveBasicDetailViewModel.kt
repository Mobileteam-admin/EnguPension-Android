package com.example.engu_pension_verification_application.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.model.input.InputActiveBasicDetails
import com.example.engu_pension_verification_application.model.input.InputLGAList
import com.example.engu_pension_verification_application.model.response.*
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
        MutableLiveData<Pair<InputActiveBasicDetails, ResponseActiveBasicDetails>>()
    val basicDetailsSubmissionResult: LiveData<Pair<InputActiveBasicDetails, ResponseActiveBasicDetails>>
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
                _basicDetailsApiResult.postValue(
                    ResponseActiveBasicRetrive(
                        ActiveRetriveDetail(message = "Something went wrong with fetching basic details")
                    )
                )
            }
        }
    }

    fun submitAccountDetails(inputActiveBasicDetails: InputActiveBasicDetails) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _basicDetailsSubmissionResult.postValue(
                    Pair(
                        inputActiveBasicDetails,
                        networkRepo.uploadAccountDetails(inputActiveBasicDetails)
                    )
                )
            } catch (e: Exception) {
                _basicDetailsSubmissionResult.postValue(
                    Pair(
                        inputActiveBasicDetails, ResponseActiveBasicDetails(
                            ActiveBasicDetail(message = "Something went wrong with basic details submission")
                        )
                    )
                )
            }
        }
    }

}