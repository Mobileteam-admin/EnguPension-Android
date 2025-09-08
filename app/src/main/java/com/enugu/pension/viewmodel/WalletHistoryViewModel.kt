package com.enugu.pension.viewmodel

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.enugu.pension.data.repository.NetworkRepo
import com.enugu.pension.data.remote.paging.TransactionHistoryPagingSource
import com.enugu.pension.data.remote.dto.response.StatementPdfLinkResponse
import com.enugu.pension.data.remote.dto.response.TransactionHistoryResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class WalletHistoryViewModel(private val networkRepo: NetworkRepo) : ViewModel() {
    lateinit var fileLocationUri: Uri
    private val _linkApiResult = MutableLiveData<StatementPdfLinkResponse>()
    val linkApiResult: LiveData<StatementPdfLinkResponse>
        get() = _linkApiResult

    private val _statementDownloadApiResult = MutableLiveData<String>()
    val statementDownloadApiResult: LiveData<String>
        get() = _statementDownloadApiResult

    val transactionFlow: Flow<PagingData<TransactionHistoryResponse.Detail.Data.Transaction>> = Pager(
        config = PagingConfig(
            pageSize = 10,
            prefetchDistance = 6,
        ),
        pagingSourceFactory = { TransactionHistoryPagingSource(networkRepo) }
    ).flow.cachedIn(viewModelScope)

    fun fetchStatementPdfLinkN() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _linkApiResult.postValue(networkRepo.fetchStatementPdfLink())
            } catch (e: Exception) {
                e.printStackTrace()
                _linkApiResult.postValue(
                    StatementPdfLinkResponse(StatementPdfLinkResponse.Detail("Something went wrong with downloading Account Statement"))
                )
            }
        }
    }

    fun downloadStatementPdf(fileUrl: String?, contentResolver: ContentResolver) {
        viewModelScope.launch(Dispatchers.IO) {
            val defaultErrorMessage = "Failed to download Account Statement"
            try {
                val message = if (fileUrl.isNullOrEmpty()) {
                    defaultErrorMessage
                } else {
                    val success = networkRepo.downloadFile(fileUrl, fileLocationUri, contentResolver)
                    if (success) "Account Statement downloaded successfully" else defaultErrorMessage
                }
                _statementDownloadApiResult.postValue(message)
            } catch (e: Exception) {
                _statementDownloadApiResult.postValue(defaultErrorMessage)
            }
        }
    }
}