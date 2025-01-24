package com.example.engu_pension_verification_application.viewmodel

import android.app.DownloadManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.data.TransactionHistoryPagingSource
import com.example.engu_pension_verification_application.model.response.StatementLinkResponse
import com.example.engu_pension_verification_application.model.response.TransactionHistoryResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class AccountStatementViewModel(private val networkRepo: NetworkRepo) : ViewModel() {
    private val _statementLinkApiResult = MutableLiveData<StatementLinkResponse>()
    val statementApiResult: LiveData<StatementLinkResponse>
        get() = _statementLinkApiResult

    val transactionFlow: Flow<PagingData<TransactionHistoryResponse.Detail.Data.Transaction>> = Pager(
        config = PagingConfig(
            pageSize = 10,
            prefetchDistance = 6,
        ),
        pagingSourceFactory = { TransactionHistoryPagingSource(networkRepo) }
    ).flow.cachedIn(viewModelScope)

    fun fetchStatementLink() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _statementLinkApiResult.postValue(networkRepo.fetchStatementLink())
            } catch (e: Exception) {
                _statementLinkApiResult.postValue(
                    StatementLinkResponse()
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