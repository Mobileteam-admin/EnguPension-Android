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
import java.io.File

class ActiveDocumentsViewModel(private val networkRepo: NetworkRepo) :ViewModel()
{
    object DocItemIndex {
        const val ID_CARD = 0
        const val PHOTO = 1
        const val APPLICATION_FORM = 2
        const val LETTER = 3
        const val CLEARANCE = 4

        fun getIndexList() = listOf(ID_CARD, PHOTO, APPLICATION_FORM, LETTER, CLEARANCE)
    }

    private val _documentsApiResult = MutableLiveData<ResponseActiveDocRetrive>()
    val documentsApiResult: LiveData<ResponseActiveDocRetrive>
        get() = _documentsApiResult

    private val _documentsUploadResult = MutableLiveData<Triple<RequestBody,ResponseActiveDocUpload,Boolean>?>(null)
    val documentsUploadResult: LiveData<Triple<RequestBody,ResponseActiveDocUpload,Boolean>?>
        get() = _documentsUploadResult

    fun uploadDocuments(requestBody: RequestBody, navigateToNext:Boolean) {
        viewModelScope.launch (Dispatchers.IO){
            try {
                _documentsUploadResult.postValue(
                    Triple(requestBody,networkRepo.uploadActiveDocuments(requestBody), navigateToNext)
                )
            } catch (e:Exception){
                _documentsUploadResult.postValue(
                    Triple(requestBody,ResponseActiveDocUpload(ActiveDocDetail( message = "Something went wrong with documents submission")), false)
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

    fun downloadDocCacheFile(fileUrl: String, cacheFile: File) {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.downloadCacheFile(fileUrl, cacheFile)
        }
    }
}