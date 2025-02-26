package com.enugu.pension.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enugu.pension.data.NetworkRepo
import com.enugu.pension.model.response.ResponseRetireeDocRetrive
import com.enugu.pension.model.response.ResponseRetireeDocUpload
import com.enugu.pension.model.response.RetireeDocDetail
import com.enugu.pension.model.response.RetireeDocRetriveResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.RequestBody
import java.io.File

class RetireeDocumentsViewModel(private val networkRepo: NetworkRepo) : ViewModel() {
    object DocItemIndex {
        const val ID_CARD = 0
        const val PASSPORT_PHOTO = 1
        const val APPLICATION_FORM = 2
        const val APPOINTMENT_FOR_PAYMENT = 3
        const val AUTHORISATION_FOR_PAYMENT = 4
        const val CLEARANCE_FORM = 5
        const val NOTIFICATION_OF_PROMOTION = 6
        const val PENSION_CERTIFICATE = 7
        const val RETIREMENT_NOTICE = 8

        fun getIndexList() = listOf(ID_CARD, PASSPORT_PHOTO, APPLICATION_FORM, APPOINTMENT_FOR_PAYMENT,
            AUTHORISATION_FOR_PAYMENT,CLEARANCE_FORM, NOTIFICATION_OF_PROMOTION, PENSION_CERTIFICATE, RETIREMENT_NOTICE,)
    }

    private val _documentsFetchResult = MutableLiveData<ResponseRetireeDocRetrive>()
    val documentsFetchResult: LiveData<ResponseRetireeDocRetrive>
        get() = _documentsFetchResult

    private val _documentsUploadResult =
        MutableLiveData<Triple<RequestBody, ResponseRetireeDocUpload, Boolean>?>(null)
    val documentsUploadResult: LiveData<Triple<RequestBody, ResponseRetireeDocUpload, Boolean>?>
        get() = _documentsUploadResult

    fun uploadDocuments(requestBody: RequestBody, navigateToNext:Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _documentsUploadResult.postValue(
                    Triple(requestBody, networkRepo.uploadRetireeDocuments(requestBody), navigateToNext)
                )
            } catch (e: Exception) {
                _documentsUploadResult.postValue(
                    Triple(
                        requestBody,
                        ResponseRetireeDocUpload(RetireeDocDetail(message = "Something went wrong with documents submission")),
                        false
                    )
                )
            }
        }
    }

    fun resetDocumentsUploadResult() {
        _documentsUploadResult.value = null
    }

    fun fetchDocuments() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _documentsFetchResult.postValue(networkRepo.fetchRetireeDocuments())
            } catch (e: Exception) {
                _documentsFetchResult.postValue(
                    ResponseRetireeDocRetrive(
                        RetireeDocRetriveResponse(
                            message = "Something went wrong with fetching documents"
                        )
                    )
                )
            }
        }
    }

    fun downloadDocCacheFile(fileUrl: String, cacheFile: File) {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.downloadCacheFile(fileUrl, cacheFile)
        }
    }
}