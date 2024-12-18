package com.example.engu_pension_verification_application.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.model.response.ActiveDocDetail
import com.example.engu_pension_verification_application.model.response.ActiveDocRetriveDetail
import com.example.engu_pension_verification_application.model.response.ResponseActiveDocRetrive
import com.example.engu_pension_verification_application.model.response.ResponseActiveDocUpload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.RequestBody

class ActiveDocumentsViewModel(private val networkRepo: NetworkRepo) :ViewModel()
{
    private val _documentsApiResult = MutableLiveData<ResponseActiveDocRetrive>()
    val documentsApiResult: LiveData<ResponseActiveDocRetrive>
        get() = _documentsApiResult

    private val _documentsUploadResult = MutableLiveData<Pair<RequestBody,ResponseActiveDocUpload>>()
    val documentsUploadResult: LiveData<Pair<RequestBody,ResponseActiveDocUpload>>
        get() = _documentsUploadResult
    fun uploadDocuments(requestBody: RequestBody) {
        viewModelScope.launch (Dispatchers.IO){
            try {
                _documentsUploadResult.postValue(
                    Pair(requestBody,networkRepo.uploadActiveDocuments(requestBody))
                )
            }catch (e:Exception){
                _documentsUploadResult.postValue(
                    Pair(requestBody,ResponseActiveDocUpload(ActiveDocDetail( message = "Something went wrong with documents submission")))
                )
            }
        }
    }
    fun fetchActiveDocuments()  {
        viewModelScope.launch (Dispatchers.IO){
            try {
                _documentsApiResult.postValue(networkRepo.fetchActiveDocuments())
            } catch (e: Exception) {
                _documentsApiResult.postValue(ResponseActiveDocRetrive(ActiveDocRetriveDetail(message = "Something went wrong with fetching documents")))
            }
        }
    }
}