package com.enugu.pension.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enugu.pension.data.NetworkRepo
import com.enugu.pension.model.response.ActiveDocDetail
import com.enugu.pension.model.response.ActiveDocRetriveDetail
import com.enugu.pension.model.response.ResponseActiveDocRetrive
import com.enugu.pension.model.response.ResponseActiveDocUpload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.RequestBody

class ActiveDocumentsViewModel(private val networkRepo: NetworkRepo) :ViewModel()
{
    private val _documentsApiResult = MutableLiveData<ResponseActiveDocRetrive>()
    val documentsApiResult: LiveData<ResponseActiveDocRetrive>
        get() = _documentsApiResult

    private val _documentsUploadResult = MutableLiveData<Pair<RequestBody,ResponseActiveDocUpload>?>(null)
    val documentsUploadResult: LiveData<Pair<RequestBody,ResponseActiveDocUpload>?>
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
    fun resetDocumentsUploadResult() {
        _documentsUploadResult.value = null
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