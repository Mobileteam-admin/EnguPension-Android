package com.example.engu_pension_verification_application.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.model.response.ResponseRetireeDocRetrive
import com.example.engu_pension_verification_application.model.response.ResponseRetireeDocUpload
import com.example.engu_pension_verification_application.model.response.RetireeDocDetail
import com.example.engu_pension_verification_application.model.response.RetireeDocRetriveResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.RequestBody

class RetireeDocumentsViewModel(private val networkRepo: NetworkRepo) : ViewModel() {
    private val _documentsFetchResult = MutableLiveData<ResponseRetireeDocRetrive>()
    val documentsFetchResult: LiveData<ResponseRetireeDocRetrive>
        get() = _documentsFetchResult

    private val _documentsUploadResult =
        MutableLiveData<Pair<RequestBody, ResponseRetireeDocUpload>>()
    val documentsUploadResult: LiveData<Pair<RequestBody, ResponseRetireeDocUpload>>
        get() = _documentsUploadResult

    fun uploadDocuments(requestBody: RequestBody) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _documentsUploadResult.postValue(
                    Pair(requestBody, networkRepo.uploadRetireeDocuments(requestBody))
                )
            } catch (e: Exception) {
                _documentsUploadResult.postValue(
                    Pair(
                        requestBody,
                        ResponseRetireeDocUpload(RetireeDocDetail(message = "Something went wrong with documents submission"))
                    )
                )
            }
        }
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
}