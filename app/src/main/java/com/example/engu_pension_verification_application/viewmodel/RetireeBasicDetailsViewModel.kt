package com.example.engu_pension_verification_application.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.model.input.InputLGAList
import com.example.engu_pension_verification_application.model.input.InputRetireeBasicDetails
import com.example.engu_pension_verification_application.model.response.*
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
        MutableLiveData<Pair<InputRetireeBasicDetails, ResponseRetireeBasicDetails>>()
    val basicDetailsSubmissionResult: LiveData<Pair<InputRetireeBasicDetails, ResponseRetireeBasicDetails>>
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
            try {
                _basicDetailsSubmissionResult.postValue(
                    Pair(
                        inputRetireeBasicDetails,
                        networkRepo.submitRetireeBasicDetails(inputRetireeBasicDetails)
                    )
                )
            } catch (e: Exception) {
                _basicDetailsSubmissionResult.postValue(
                    Pair(
                        inputRetireeBasicDetails, ResponseRetireeBasicDetails(
                            RetireeBasicDetail(message = "Something went wrong with basic details submission")
                        )
                    )
                )
            }
        }
    }
}