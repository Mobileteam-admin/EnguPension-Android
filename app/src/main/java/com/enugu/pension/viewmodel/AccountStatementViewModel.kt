package com.enugu.pension.viewmodel

import android.app.DownloadManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.enugu.pension.data.NetworkRepo
import com.enugu.pension.data.TransactionHistoryPagingSource
import com.enugu.pension.model.response.StatementPdfLinkResponse
import com.enugu.pension.model.response.TransactionHistoryResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class AccountStatementViewModel(private val networkRepo: NetworkRepo) : ViewModel() {
    private val _statementLinkApiResult = MutableLiveData<StatementPdfLinkResponse>()
    val statementApiResult: LiveData<StatementPdfLinkResponse>
        get() = _statementLinkApiResult

    val transactionFlow: Flow<PagingData<TransactionHistoryResponse.Detail.Data.Transaction>> = Pager(
        config = PagingConfig(
            pageSize = 10,
            prefetchDistance = 6,
        ),
        pagingSourceFactory = { TransactionHistoryPagingSource(networkRepo) }
    ).flow.cachedIn(viewModelScope)

    fun fetchStatementPdfLink() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _statementLinkApiResult.postValue(networkRepo.fetchStatementPdfLink())
            } catch (e: Exception) {
                _statementLinkApiResult.postValue(
                    StatementPdfLinkResponse()
                )
            }
        }
    }

    fun downloadPdf(
        downloadManager: DownloadManager,
        fileUrl: String, fileName: String,
        downloadDescription: String
    ){
        networkRepo.downloadFile(downloadManager, fileUrl, fileName, downloadDescription)
    }
}